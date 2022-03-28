/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2022 HappyAndJust
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.happyandjust.nameless.gui.graph

import com.happyandjust.nameless.dsl.withPrecisionText
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.State
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.vigilance.utils.onLeftClick
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.objecthunter.exp4j.ExpressionBuilder
import org.lwjgl.opengl.GL11
import java.util.*
import kotlin.properties.Delegates

class GraphComponent(window: Window, private var expression: String) : UIComponent() {

    private var lastPosition: Pair<Float, Float>? = null
    private var captureCenter = 0.0 to 0.0

    init {
        effect(ScissorEffect(this))

        onLeftClick {
            if (it.relativeX in 0f..getWidth() && it.relativeY in 0f..getHeight()) {
                lastPosition = it.relativeX to it.relativeY
                captureCenter = screenCenter.copy()
            }
        }

        onMouseRelease {
            if (lastPosition != null) {
                lastPosition = null

                updateExpression(expression)
            }
        }

        onMouseDrag { mouseX, mouseY, mouseButton ->
            if (mouseButton == 0) {
                lastPosition?.let {

                    val (moveX, moveY) = (mouseX + getLeft() - it.first) to (mouseY + getTop() - it.second)

                    screenCenter =
                        (captureCenter.first - moveX * screenScale) to (captureCenter.second + moveY * screenScale)
                }
            }
        }

        onMouseScroll {
            val newScale = screenScale - it.delta / 100
            screenScale = newScale.coerceAtLeast(0.002)
        }
    }

    fun updateExpression(expression: String) = apply {
        this.expression = expression
        points.clear()
    }

    private var calculating = false
    private val points = Collections.synchronizedSet(hashSetOf<Pair<Double, Double>>())
    private val errorExpressions = hashSetOf<String>()

    private var screenScale: Double by Delegates.observable(1.0) { _, _, _ ->
        rebindTexts()
    }
    private var screenCenter: Pair<Double, Double> by Delegates.observable(0.0 to 0.0) { _, _, _ ->
        rebindTexts()
    }
    private val screenWidth
        get() = getWidth() * screenScale
    private val screenHeight
        get() = getHeight() * screenScale

    private val screenLeft
        get() = screenCenter.first - screenWidth / 2
    private val screenTop
        get() = screenCenter.second - screenHeight / 2
    private val screenRight
        get() = screenLeft + screenWidth
    private val screenBottom
        get() = screenTop + screenHeight

    private fun Double.t() = withPrecisionText(1)

    private val lambdaStates = arrayListOf(
        // screenTop < screenBottom
        LambdaState { "(${screenLeft.t()}, ${screenBottom.t()})" },
        LambdaState { "(${screenRight.t()}, ${screenBottom.t()})" },
        LambdaState { "(${screenLeft.t()}, ${screenTop.t()})" },
        LambdaState { "(${screenRight.t()}, ${screenTop.t()})" }
    )

    private val leftTopText = UIText().constrain {
        textScale = 1.5.pixels()
    } childOf this

    private val rightTopText = UIText().constrain {
        x = 0.pixel(true)

        textScale = 1.5.pixels()
    } childOf this

    private val leftBottomText = UIText().constrain {

        y = 0.pixel(true)

        textScale = 1.5.pixels()
    } childOf this

    private val rightBottomText = UIText().constrain {
        x = 0.pixel(true)
        y = 0.pixel(true)

        textScale = 1.5.pixels()
    } childOf this

    private fun rebindTexts() = lambdaStates.forEach { it.rebind() }

    init {
        Window.enqueueRenderOperation {
            screenCenter = (-window.getLeft() / 2.0) to (-window.getTop() / 2.0)

            leftTopText.bindText(lambdaStates[0])
            rightTopText.bindText(lambdaStates[1])
            leftBottomText.bindText(lambdaStates[2])
            rightBottomText.bindText(lambdaStates[3])
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun recalculate() {
        if (expression.isBlank()) return
        if (expression in errorExpressions) return
        calculating = true

        GlobalScope.launch {
            runCatching {
                val builder = ExpressionBuilder(expression)
                    .variable("x")
                    .build()

                val left = screenLeft
                val right = screenRight

                val interval = (right - left + 1) / pointNumber

                repeat(pointNumber) {
                    runCatching { // divide by zero
                        val x = left + interval * it
                        val y = builder.setVariable("x", x).evaluate()

                        if (y !in screenTop..screenBottom) return@repeat

                        points.add(x to y)
                    }
                }
            }.onFailure {
                it.printStackTrace()

                errorExpressions.add(expression)
            }

            calculating = false
        }
    }

    override fun draw(matrixStack: UMatrixStack) {
        beforeDraw(matrixStack)

        val left = getLeft().toDouble()
        val top = getTop().toDouble()

        val wr = UGraphics.getFromTessellator()

        UGraphics.enableBlend()
        UGraphics.disableTexture2D()
        UGraphics.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        UGraphics.GL.translate(left, top, 0.0)

        GL11.glPointSize(1.5f)
        GL11.glLineWidth(2f)

        wr.drawAxis()

        if (points.isEmpty() && !calculating) {
            recalculate()
        } else {
            wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION)
            GL11.glEnable(GL11.GL_POINT_SMOOTH)

            UGraphics.color4f(1f, 1f, 1f, 1f)

            synchronized(points) {
                for ((x, y) in points) {
                    wr.pos(matrixStack, x.renderX, y.renderY, 0.0).endVertex()
                }
            }

            wr.drawDirect()
            GL11.glDisable(GL11.GL_POINT_SMOOTH)
        }

        UGraphics.GL.translate(-left, -top, 0.0)
        UGraphics.disableBlend()
        UGraphics.enableTexture2D()

        super.draw(matrixStack)
    }

    private fun UGraphics.drawAxis() {
        begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

        UGraphics.color4f(0f, 1f, 0f, 1f)

        pos(0.0, 0.0.renderY, 0.0).endVertex()
        pos(getWidth().toDouble(), 0.0.renderY, 0.0).endVertex()

        pos(0.0.renderX, 0.0, 0.0).endVertex()
        pos(0.0.renderX, getHeight().toDouble(), 0.0).endVertex()

        UGraphics.draw()
    }

    private val Double.renderX
        get() = (this - screenLeft) / (screenWidth / getWidth())
    private val Double.renderY
        get() = getHeight() - ((this - screenTop) / (screenHeight / getHeight()))

    companion object {
        private const val pointNumber = 400000
    }

    private class LambdaState<T>(private val getter: () -> T) : State<T>() {

        private var value = getter()

        fun rebind() {
            value = getter()
            set(value)
        }

        override fun get() = value

    }
}