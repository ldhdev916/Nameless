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
import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.mid
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.property.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import com.happyandjust.nameless.textureoverlay.Overlay

class ECategory(rectangle: Rectangle, val category: Category) : EPanel(rectangle) {

    private val categoryLabel =
        ELabel(rectangle, Alignment.CENTER, Alignment.CENTER, category.displayName).also { addChild(it) }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        val hovered = rectangle.isMouseInRectangle(mouseX, mouseY)

        if (hovered) {
            rectangle.drawRect(0xFF595959.toInt())
        }
    }

    override fun onRectangleUpdate() {
        categoryLabel.rectangle = rectangle
    }
}

class EFeature(rectangle: Rectangle, val feature: SimpleFeature, val onClick: (EFeature) -> Unit) :
    EPanel(rectangle) {

    private val titlePanel = ELabel(
        Rectangle.fromWidthHeight(
            rectangle.left + 10,
            rectangle.top,
            rectangle.width,
            mc.fontRendererObj.FONT_HEIGHT * 4
        ),
        Alignment.LEFT,
        Alignment.CENTER,
        feature.title,
        1.5
    ).also { addChild(it) }
    private val descPanel = ELabel(
        Rectangle.fromWidthHeight(
            titlePanel.rectangle.left,
            titlePanel.rectangle.bottom,
            titlePanel.rectangle.width,
            rectangle.height - titlePanel.rectangle.height
        ),
        Alignment.LEFT,
        Alignment.TOP,
        feature.desc
    ).also { addChild(it) }
    private val toggleButton: EToggleButton
    private var settingButton: EButton? = null
    private var relocateButton: ERelocateButton? = null

    init {

        val toggleWidth = 28
        val toggleHeight = 14

        val buttonBorder = 30

        toggleButton = EToggleButton(
            Rectangle.fromWidthHeight(rectangle.right - buttonBorder - toggleWidth, 0, toggleWidth, toggleHeight),
            feature.enabled
        ) { feature.enabled = it }

        addChild(toggleButton)

        var lastLeft = toggleButton.rectangle.left

        if (feature.parameters.isNotEmpty()) {
            val settingWidth = 60
            val settingHeight = toggleHeight

            settingButton = EButton(
                Rectangle.fromWidthHeight(
                    lastLeft - buttonBorder - settingWidth,
                    toggleButton.rectangle.top,
                    settingWidth,
                    settingHeight
                ), "Setting", true
            ).also {
                it.onClick = {
                    onClick(this)
                }
                lastLeft = it.rectangle.left

                addChild(it)
            }

        }

        if (feature is OverlayFeature) {
            val relocateWidth = 60
            val relocateHeight = toggleHeight

            relocateButton = ERelocateButton(
                Rectangle.fromWidthHeight(
                    lastLeft - buttonBorder - relocateWidth,
                    toggleButton.rectangle.top,
                    relocateWidth,
                    relocateHeight,
                ),
                feature.getRelocateGui()
            ).also {
                relocateButton = it
                lastLeft = it.rectangle.left

                addChild(it)
            }
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        rectangle.drawRect(0xFF1E1E1E.toInt())
    }

    override fun onRectangleUpdate() {

        titlePanel.rectangle =
            titlePanel.rectangle.copy(top = rectangle.top, bottom = rectangle.top + titlePanel.rectangle.height)

        descPanel.rectangle = descPanel.rectangle.copy(
            top = titlePanel.rectangle.bottom,
            bottom = titlePanel.rectangle.bottom + descPanel.rectangle.height
        )

        val toggleTop = mid(rectangle.top, rectangle.bottom) - (toggleButton.rectangle.height / 2)

        toggleButton.rectangle = toggleButton.rectangle.copy(
            top = toggleTop,
            bottom = toggleTop + toggleButton.rectangle.height
        )

        settingButton?.let {
            it.rectangle =
                it.rectangle.copy(top = toggleButton.rectangle.top, bottom = toggleButton.rectangle.bottom)
        }

        relocateButton?.let {
            it.rectangle = it.rectangle.copy(top = toggleButton.rectangle.top, bottom = toggleButton.rectangle.bottom)
        }

    }
}

class EParameter(rectangle: Rectangle, val parameter: FeatureParameter<*>, val onClick: (EParameter) -> Unit) :
    EPanel(rectangle) {

    private val titlePanel = ELabel(
        Rectangle.fromWidthHeight(
            rectangle.left + 10,
            rectangle.top,
            rectangle.width,
            mc.fontRendererObj.FONT_HEIGHT * 4
        ),
        Alignment.LEFT,
        Alignment.CENTER,
        parameter.title,
        1.5
    ).also { addChild(it) }
    private val descPanel = ELabel(
        Rectangle.fromWidthHeight(
            titlePanel.rectangle.left,
            titlePanel.rectangle.bottom,
            titlePanel.rectangle.width,
            rectangle.height - titlePanel.rectangle.height
        ),
        Alignment.LEFT,
        Alignment.TOP,
        parameter.desc
    ).also { addChild(it) }
    private val parameterPanel: EPanel
    private var settingButton: EButton? = null

    init {
        val property = when (parameter.defaultValue) {
            is Int -> IntProperty(parameter as FeatureParameter<Int>)
            is Double -> DoubleProperty(parameter as FeatureParameter<Double>)
            is Boolean -> BooleanProperty(parameter as FeatureParameter<Boolean>)
            is String -> StringProperty(parameter as FeatureParameter<String>)
            is ChromaColor -> ChromaColorProperty(parameter as FeatureParameter<ChromaColor>)
            is Overlay -> OverlayProperty(parameter as FeatureParameter<Overlay>)
            is Enum<*> -> EnumProperty(parameter as FeatureParameter<Enum<*>>)
            else -> throw IllegalArgumentException("Unsupported Property")
        }

        val buttonBorder = 30

        parameterPanel = property.panel

        parameterPanel.rectangle = Rectangle.fromWidthHeight(
            rectangle.right - buttonBorder - parameterPanel.rectangle.width,
            0,
            parameterPanel.rectangle.width,
            parameterPanel.rectangle.height
        )

        addChild(parameterPanel)

        var lastLeft = parameterPanel.rectangle.left

        if (parameter.parameters.isNotEmpty()) {
            val settingWidth = 60
            val settingHeight = 14

            settingButton = EButton(
                Rectangle.fromWidthHeight(
                    lastLeft - buttonBorder - settingWidth,
                    mid(rectangle.top, rectangle.bottom) - (settingHeight / 2),
                    settingWidth,
                    settingHeight
                ), "Setting", true
            ).also {
                it.onClick = {
                    onClick(this)
                }
                lastLeft = it.rectangle.left

                addChild(it)
            }
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        rectangle.drawRect(0xFF3F3F3F.toInt())
    }

    override fun onRectangleUpdate() {
        titlePanel.rectangle =
            titlePanel.rectangle.copy(top = rectangle.top, bottom = rectangle.top + titlePanel.rectangle.height)

        descPanel.rectangle = descPanel.rectangle.copy(
            top = titlePanel.rectangle.bottom,
            bottom = titlePanel.rectangle.bottom + descPanel.rectangle.height
        )

        val paramPanelTop = mid(rectangle.top, rectangle.bottom) - (parameterPanel.rectangle.height / 2)

        parameterPanel.rectangle = parameterPanel.rectangle.copy(
            top = paramPanelTop,
            bottom = paramPanelTop + parameterPanel.rectangle.height
        )

        val mid = mid(rectangle.top, rectangle.bottom)

        settingButton?.let {
            it.rectangle =
                it.rectangle.copy(top = mid - (it.rectangle.height / 2), bottom = mid + (it.rectangle.height / 2))
        }
    }

}
