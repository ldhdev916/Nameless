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

import com.happyandjust.nameless.core.Alignment
import com.happyandjust.nameless.core.Direction
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.floor
import kotlin.math.round

open class EBasicSlider(
    rectangle: Rectangle,
    private val minValue: Double,
    private val maxValue: Double,
    currentValue: Double,
    private val precision: Int,
    protected var storeValue: (Double) -> Unit
) : EPanel(rectangle) {

    protected var sliderValue = (currentValue - minValue) / (maxValue - minValue)
        set(value) {
            field = value.compress(0.0, 1.0)
            storeValue(getValue())
        }

    private val textColor = 0xFF969696.toInt()
    protected open var rectColor = 0xFF01A552.toInt()
    protected open var startColor = 0xFF01A552.toInt()
    protected open var endColor = 0xFF00793A.toInt()
    var dragging = false

    private val minLabel =
        ELabel(
            rectangle.offset(-(rectangle.width + getRectangleWidthHeight() / 2), 0),
            Alignment.RIGHT,
            Alignment.CENTER,
            "",
            0.7,
            textColor
        ).also { addChild(it) }
    private val currentLabel =
        ELabel(
            rectangle.offset(0, rectangle.height),
            Alignment.CENTER,
            Alignment.TOP,
            "",
            0.7,
            textColor
        ).also { addChild(it) }
    private val maxLabel =
        ELabel(
            rectangle.offset(rectangle.width + getRectangleWidthHeight() / 2, 0),
            Alignment.LEFT,
            Alignment.CENTER,
            "",
            0.7,
            textColor
        ).also { addChild(it) }
    private val movingRect =
        object : EPanel(Rectangle.fromWidthHeight(0, 0, getRectangleWidthHeight(), getRectangleWidthHeight())) {

            override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
                this.rectangle.drawOutlineBox(Color.black.rgb, 1)

                this.rectangle.expand(-1, -1).drawRect(rectColor)

            }

            override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
                if (doubleClicked) onRectangleDoubleClicked()
            }

        }.also { addChild(it) }

    /**
     * Enable Chroma
     */
    protected open fun onRectangleDoubleClicked() {}

    override fun onRectangleUpdate() {
        minLabel.rectangle = rectangle.offset(-(rectangle.width + getRectangleWidthHeight() / 2), 0)

        maxLabel.rectangle = rectangle.offset(rectangle.width + getRectangleWidthHeight() / 2, 0)
    }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (isClickedInside) {
            dragging = true

            sliderValue = (mouseX - rectangle.left) / rectangle.width.toDouble()
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        if (!Mouse.isButtonDown(0)) {
            dragging = false
        }

        if (dragging) {
            sliderValue = (mouseX - (rectangle.left + 1)) / (rectangle.width - 2).toDouble()
        }

        val minValueString = (if (precision == 0) minValue.toInt() else trimToPrecision(minValue)).toString()
        val maxValueString = (if (precision == 0) maxValue.toInt() else trimToPrecision(maxValue)).toString()

        minLabel.text = minValueString
        maxLabel.text = maxValueString

        minLabel.textColor = textColor
        maxLabel.textColor = textColor


        val x = (rectangle.left + rectangle.width * sliderValue).toInt()
        val y = mid(rectangle.top, rectangle.bottom)

        val text = (if (precision == 0) getValueInt() else trimToPrecision(getValue())).toString()

        val textWidth = mc.fontRendererObj.getStringWidth(text)

        currentLabel.rectangle =
            Rectangle(
                x - textWidth,
                rectangle.bottom + (getRectangleWidthHeight() / 2),
                x + textWidth,
                rectangle.bottom + rectangle.height + (getRectangleWidthHeight() / 2)
            )
        currentLabel.text = text
        currentLabel.textColor = textColor

        movingRect.rectangle = Rectangle.fromWidthHeight(
            x - (getRectangleWidthHeight() / 2),
            y - (getRectangleWidthHeight() / 2),
            movingRect.rectangle.width,
            movingRect.rectangle.height
        )

        drawRects(scale)
    }

    private fun drawRects(scale: Int) {

        scissor(rectangle.copy(right = movingRect.rectangle.left), scale) {
            drawGradientRect(rectangle)
        }

        rectangle.drawOutlineBox(textColor)

    }

    open fun drawGradientRect(rectangle: Rectangle) {
        rectangle.drawGradientRect(Direction.RIGHT, startColor, endColor)
    }

    private fun getRectangleWidthHeight() = rectangle.height + 8


    fun getValue() = minValue + (maxValue - minValue) * sliderValue

    fun getValueInt() = round(getValue()).toInt()

    private fun trimToPrecision(value: Double): Double {
        val pow = 10.pow(precision)

        return floor(value * pow) / pow
    }
}