/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2021 HappyAndJust
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

package com.happyandjust.nameless.gui.elements

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color

class EChromaColorSlider(
    rectangle: Rectangle,
    currentColor: ChromaColor,
    private val storeColor: (ChromaColor) -> Unit
) : EBasicSlider(
    rectangle,
    0.0,
    1.0,
    Color.RGBtoHSB(currentColor.red, currentColor.green, currentColor.blue, null)[0].toDouble(),
    2,
    {}
) {

    init {
        storeValue = {
            storeColor(ChromaColor(Color.HSBtoRGB(it.toFloat(), 1f, 1f)).also { color ->
                color.chromaEnabled = chromaEnabled
            })
        }
    }

    var chromaEnabled: Boolean = currentColor.chromaEnabled

    override var rectColor: Int = currentColor.originRGB
        get() = Color.HSBtoRGB(getValue().toFloat(), 1f, 1f)

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {

        if (chromaEnabled) {
            sliderValue += 0.0038F

            if (sliderValue == 1.0) {
                sliderValue = 0.0
            }
        }

        super.draw(mouseX, mouseY, scale)
    }

    override fun drawGradientRect(rectangle: Rectangle) {
        val addEach = rectangle.width / 360.0

        val wr = tessellator.worldRenderer

        disableTexture2D()
        enableBlend()
        tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        translate(rectangle.left, rectangle.top, 0) {

            wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR)

            for (i in 0..360) {
                val c = Color.HSBtoRGB(i / 360F, 1f, 1f)

                val x = addEach * i

                wr.pos(x, 0.0, 0.0).color(c).endVertex()
                wr.pos(x, rectangle.height.toDouble(), 0.0).color(c).endVertex()
            }

            tessellator.draw()
        }



        disableBlend()
        enableTexture2D()
    }

    override fun onRectangleDoubleClicked() {
        chromaEnabled = true
    }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        super.mousePressed(mouseX, mouseY, isClickedInside, doubleClicked)

        if (!doubleClicked && isClickedInside) {
            chromaEnabled = false
        }
    }
}