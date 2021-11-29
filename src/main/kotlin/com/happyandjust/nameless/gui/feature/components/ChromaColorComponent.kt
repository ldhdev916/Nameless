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

package com.happyandjust.nameless.gui.feature.components

import com.happyandjust.nameless.core.toChromaColor
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.gui.settings.ColorComponent
import gg.essential.vigilance.gui.settings.ColorPicker
import gg.essential.vigilance.gui.settings.SettingComponent
import java.awt.Color

class ChromaColorComponent(private var chromaEnabled: Boolean, colorComponent: ColorComponent) : SettingComponent() {

    private val colorPicker: ColorPicker

    init {

        constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        colorComponent childOf this

        colorComponent.constraints.x = 0.pixel(alignOpposite = true)

        colorComponent.onValueChange {
            changeValue((it as Color).toChromaColor(chromaEnabled))
        }

        colorPicker = ColorComponent::class.java.getDeclaredMethod("getColorPicker")
            .also { it.isAccessible = true }(colorComponent) as ColorPicker

        colorPicker.constrain {
            height += chromaContainerHeight
        }

        adjustColorPickerChildren()
    }

    private fun adjustColorPickerChildren() {
        with(ColorPicker::class.java) {
            val bigPickerBox =
                getDeclaredField("bigPickerBox").also { it.isAccessible = true }[colorPicker] as UIComponent
            val huePickerLine =
                getDeclaredField("huePickerLine").also { it.isAccessible = true }[colorPicker] as UIComponent

            val alphaSlider =
                getDeclaredField("alphaSlider").also { it.isAccessible = true }[colorPicker] as UIComponent

            bigPickerBox.constrain {
                height -= chromaContainerHeight
            }
            huePickerLine.constrain {
                height -= chromaContainerHeight
            }
            alphaSlider.constrain {
                height -= chromaContainerHeight
            }
        }
    }

    private val chromaEnabledContainer by UIContainer().constrain {
        y = SiblingConstraint(5f)

        width = 100.percent()
        height = chromaContainerHeight
    } childOf colorPicker

    init {
        UIText("Enable Chroma").constrain {
            y = CenterConstraint()
        } childOf chromaEnabledContainer

        val checkBox = CheckboxComponent(chromaEnabled).constrain {
            x = 0.pixel(alignOpposite = true)
            y = CenterConstraint()

            width = chromaContainerHeight * 0.8
            height = AspectConstraint()
        } childOf chromaEnabledContainer

        val checkmark = CheckboxComponent::class.java.getDeclaredField("checkmark")
            .also { it.isAccessible = true }[checkBox] as UIImage

        checkmark.constrain {
            width *= 0.7
            height *= 0.7
        }

        checkBox.onValueChange {
            chromaEnabled = it as Boolean

            changeValue(colorPicker.getCurrentColor().toChromaColor(chromaEnabled))
        }
    }

    companion object {
        val chromaContainerHeight = 14.pixels()
    }

}

fun ColorComponent.toChromaColorComponent(chromaEnabled: Boolean) = ChromaColorComponent(chromaEnabled, this)