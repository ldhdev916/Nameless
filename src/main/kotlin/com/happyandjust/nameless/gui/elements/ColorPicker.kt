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
import com.happyandjust.nameless.core.Direction
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class EColorPicker(rectangle: Rectangle, currentColor_: ChromaColor, private val storeColor: (ChromaColor) -> Unit) :
    EPanel(rectangle) {

    private val DEGREE_TO_RADIANS = PI / 180F
    var currentColor = currentColor_
        set(value) {
            if (field != value) {
                field = value
                storeColor(value)

                val colorPickers = arrayListOf<IColorPicker>(
                    hexTextField
                ).also {
                    it.addAll(rgbSliders)
                    it.addAll(hsbSliders)
                }

                when (colorPickerTypeSelector.currentEnumType) {
                    ColorPickerType.RGB -> colorPickers.removeAll(rgbSliders)
                    ColorPickerType.HEX -> colorPickers.remove(hexTextField)
                    ColorPickerType.HSB -> colorPickers.removeAll(hsbSliders)
                }

                for (colorPicker in colorPickers) {
                    colorPicker.changeColor(value.rgb)
                }
            }
        }

    private val colorPickerTypeSelector = EEnumSelectPanel(
        Rectangle.fromWidthHeight(rectangle.left, rectangle.top, rectangle.width, selectorHeight),
        if (currentColor.chromaEnabled) ColorPickerType.CHROMA else ColorPickerType.RGB,
        ColorPickerType.values().toList()
    )
    private val selectorHeight
        get() = rectangle.height / 16
    private val slidersHeight: Int
        get() = rectangle.height / 40
    private val elementWidth: Int
        get() = (rectangle.width * 0.8).toInt()
    private val rgbSliders = arrayListOf<ERGBSlider>().apply {

        var multiplier = 0.7

        for (rgbType in RGBType.values()) {
            ERGBSlider(
                Rectangle.fromWidthHeight(
                    mid(rectangle.left, rectangle.right) - (elementWidth / 2),
                    rectangle.top + (rectangle.height * multiplier).toInt(),
                    elementWidth,
                    slidersHeight
                ),
                this@EColorPicker,
                rgbType
            ).also {
                add(it)
            }
            multiplier += 0.1
        }
    }
    private val hsbSliders = arrayListOf<EHSBSlider>().apply {

        var multiplier = 0.7

        for (hsbType in HSBType.values()) {
            EHSBSlider(
                Rectangle.fromWidthHeight(
                    mid(rectangle.left, rectangle.right) - (elementWidth / 2),
                    rectangle.top + (rectangle.height * multiplier).toInt(),
                    elementWidth,
                    slidersHeight
                ),
                this@EColorPicker,
                hsbType
            ).also {
                add(it)
            }
            multiplier += 0.1
        }
    }
    private val hexTextField = EHexTextField(
        Rectangle.fromWidthHeight(
            mid(rectangle.left, rectangle.right) - (elementWidth / 2),
            rectangle.top + (rectangle.height * 0.8).toInt(),
            elementWidth,
            rectangle.height / 8
        ),
        this
    )

    init {
        addChild(colorPickerTypeSelector)
        if (!currentColor.chromaEnabled) {
            rgbSliders.forEach {
                addChild(it)
            }
        }

        val rgb = currentColor.rgb

        rgbSliders.forEach {
            it.changeColor(rgb)
        }
        hexTextField.changeColor(rgb)
        hsbSliders.forEach {
            it.changeColor(rgb)
        }
    }

    override fun onRectangleUpdate() {
        colorPickerTypeSelector.rectangle = Rectangle.fromWidthHeight(
            rectangle.left,
            rectangle.top,
            rectangle.width,
            selectorHeight
        )

        colorPickerTypeSelector.onCurrentEnumChange = { type ->

            rgbSliders.forEach { removeChild(it) }
            hsbSliders.forEach { removeChild(it) }
            removeChild(hexTextField)
            currentColor.chromaEnabled = false

            when (type) {
                ColorPickerType.RGB -> rgbSliders.forEach { addChild(it) }
                ColorPickerType.HEX -> addChild(hexTextField)
                ColorPickerType.HSB -> hsbSliders.forEach { addChild(it) }
                ColorPickerType.CHROMA -> currentColor = currentColor.toChromaColor(true)
            }
        }

        run {
            var mutliplier = 0.7

            for (rgbSlider in rgbSliders) {
                rgbSlider.rectangle = Rectangle.fromWidthHeight(
                    mid(rectangle.left, rectangle.right) - (elementWidth / 2),
                    rectangle.top + (rectangle.height * mutliplier).toInt(),
                    elementWidth,
                    slidersHeight
                )

                mutliplier += 0.1
            }
        }

        run {
            var mutliplier = 0.7

            for (hsbSlider in hsbSliders) {
                hsbSlider.rectangle = Rectangle.fromWidthHeight(
                    mid(rectangle.left, rectangle.right) - (elementWidth / 2),
                    rectangle.top + (rectangle.height * mutliplier).toInt(),
                    elementWidth,
                    slidersHeight
                )

                mutliplier += 0.1
            }
        }

        hexTextField.rectangle = Rectangle.fromWidthHeight(
            mid(rectangle.left, rectangle.right) - (elementWidth / 2),
            rectangle.top + (rectangle.height * 0.8).toInt(),
            elementWidth,
            rectangle.height / 8
        )
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {

        val isChroma = currentColor.chromaEnabled
        val radius = rectangle.height / 5
        val centerX = mid(rectangle.left, rectangle.right)
        val centerY = mid(colorPickerTypeSelector.rectangle.bottom, rgbSliders[0].rectangle.top)

        disableTexture2D()
        enableBlend()
        tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )
        disableCull()
        if (isChroma) shadeModel(GL11.GL_SMOOTH)

        val wr = tessellator.worldRenderer

        if (!isChroma) color(currentColor.rgb)

        wr.begin(GL11.GL_POLYGON, if (isChroma) DefaultVertexFormats.POSITION_COLOR else DefaultVertexFormats.POSITION)

        repeat(360) {
            val x = radius * cos(it * DEGREE_TO_RADIANS)
            val y = radius * sin(it * DEGREE_TO_RADIANS)

            wr.pos(x + centerX, y + centerY, 0.0).run {
                if (isChroma) color(Color.HSBtoRGB(it / 360F, 1F, 1F)) else this
            }.endVertex()
        }

        tessellator.draw()

        if (isChroma) shadeModel(GL11.GL_FLAT)
        enableCull()
        disableBlend()
        enableTexture2D()
    }
}

class EHexTextField(rectangle: Rectangle, private val parent: EColorPicker) : ETextField(rectangle, 2.0), IColorPicker {

    init {
        validator = { it.isEmpty() || it.toIntOrNull(16) != null }
        maxStringWidth = 6

        onKeyTyped = {
            if (text.length != 6) {
                parent.currentColor = Color.black.toChromaColor(parent.currentColor.chromaEnabled)
            } else {
                parent.currentColor = Color(text.toInt(16)).toChromaColor(parent.currentColor.chromaEnabled)
            }
        }
    }

    override fun changeColor(color: Int) {
        text = color.toHexString().substring(2)
    }
}

class ERGBSlider(rectangle: Rectangle, private val parent: EColorPicker, private val rgbType: RGBType) : EBasicSlider(
    rectangle,
    0.0,
    255.0,
    0.0,
    0,
    {
        with(parent.currentColor) {
            parent.currentColor = when (rgbType) {
                RGBType.RED -> Color(it.toInt(), green, blue)
                RGBType.GREEN -> Color(red, it.toInt(), blue)
                RGBType.BLUE -> Color(red, green, it.toInt())
            }.toChromaColor(chromaEnabled)
        }
    }
), IColorPicker {

    override var rectColor = when (rgbType) {
        RGBType.RED -> Color(194, 29, 1).rgb
        RGBType.GREEN -> Color(1, 165, 82).rgb
        RGBType.BLUE -> Color(35, 2, 190).rgb
    }

    override var endColor = when (rgbType) {
        RGBType.RED -> Color(140, 23, 0).rgb
        RGBType.GREEN -> Color(0, 121, 58).rgb
        RGBType.BLUE -> Color(27, 0, 148).rgb
    }

    override var startColor = rectColor

    override fun changeColor(color: Int) {
        sliderValue = when (rgbType) {
            RGBType.RED -> color.getRedInt()
            RGBType.GREEN -> color.getGreenInt()
            RGBType.BLUE -> color.getBlueInt()
        } / 255.0
    }
}

class EHSBSlider(rectangle: Rectangle, private val parent: EColorPicker, private val hsbType: HSBType) : EBasicSlider(
    rectangle,
    0.0,
    1.0,
    0.0,
    2,
    {
        with(parent.currentColor) {

            val hue = rgb.getHue()
            val saturation = rgb.getSaturation()
            val brightness = rgb.getBrightness()

            parent.currentColor = Color(
                when (hsbType) {
                    HSBType.HUE -> Color.HSBtoRGB(it.toFloat(), saturation, brightness)
                    HSBType.SATURATION -> Color.HSBtoRGB(hue, it.toFloat(), brightness)
                    HSBType.BRIGHTNESS -> Color.HSBtoRGB(hue, saturation, it.toFloat())
                }
            ).toChromaColor(chromaEnabled)
        }
    }
), IColorPicker {
    override var rectColor = 0
        get() = with(parent.currentColor.rgb) {
            when (hsbType) {
                HSBType.HUE -> Color.HSBtoRGB(getHue(), 1F, 1F)
                HSBType.SATURATION -> Color.HSBtoRGB(getHue(), getSaturation(), 1F)
                HSBType.BRIGHTNESS -> Color.HSBtoRGB(getHue(), 0F, getBrightness())
            }
        }

    override var startColor = 0
        get() = with(parent.currentColor.rgb) {
            when (hsbType) {
                HSBType.HUE -> 0
                HSBType.SATURATION -> Color.HSBtoRGB(getHue(), 0F, 1F)
                HSBType.BRIGHTNESS -> Color.HSBtoRGB(getHue(), 0F, 0F)
            }
        }

    override var endColor = 0
        get() = with(parent.currentColor.rgb) {
            when (hsbType) {
                HSBType.HUE -> 0
                HSBType.SATURATION -> Color.HSBtoRGB(getHue(), 1F, 1F)
                HSBType.BRIGHTNESS -> Color.HSBtoRGB(getHue(), 0F, 1F)
            }
        }

    override fun drawGradientRect(rectangle: Rectangle) {
        if (hsbType == HSBType.HUE) {
            rectangle.drawChromaRect(Direction.RIGHT)
        } else {
            super.drawGradientRect(rectangle)
        }
    }

    override fun changeColor(color: Int) {
        sliderValue = when (hsbType) {
            HSBType.HUE -> color.getHue()
            HSBType.SATURATION -> color.getSaturation()
            HSBType.BRIGHTNESS -> color.getBrightness()
        }.toDouble()
    }
}

enum class RGBType {
    RED, GREEN, BLUE
}

enum class HSBType {
    HUE, SATURATION, BRIGHTNESS
}

enum class ColorPickerType {
    RGB, HEX, HSB, CHROMA
}

interface IColorPicker {
    fun changeColor(color: Int)
}