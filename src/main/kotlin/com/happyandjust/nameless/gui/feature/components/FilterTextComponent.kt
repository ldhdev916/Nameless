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

package com.happyandjust.nameless.gui.feature.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.components.input.AbstractTextInput
import gg.essential.elementa.components.input.UIMultilineTextInput
import gg.essential.elementa.components.input.UIPasswordInput
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.CopyConstraintFloat
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.gui.settings.TextComponent
import java.awt.Color

class FilterTextComponent(private val textComponent: TextComponent) : SettingComponent() {


    private var isWarningAppeared = false
    var validator: (Char) -> Boolean = { true }

    init {
        constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }
        textComponent.constrain {
            x = 0.pixel(alignOpposite = true)

            width = ChildBasedMaxSizeConstraint()
        }
    }

    override fun setupParentListeners(parent: UIComponent) {

        when (val textInput = TextComponent::class.java.getDeclaredField("textInput")
            .also { it.isAccessible = true }[textComponent] as AbstractTextInput) {
            is UIMultilineTextInput -> {
                textInput.constraints.width = basicWidthConstraint { this.parent.getWidth() * 0.4f }
            }
            is UIPasswordInput, is UITextInput -> {
                (textInput as UITextInput).setMaxWidth(basicWidthConstraint { this.parent.getWidth() * 0.5f })
            }
        }
        textComponent childOf this

        val textHolder = TextComponent::class.java.getDeclaredField("textHolder")
            .also { it.isAccessible = true }[textComponent] as UIBlock

        val warning by UIWrappedText().constrain {

            y = SiblingConstraint()

            width = CopyConstraintFloat() boundTo textHolder

            color = Color.red.constraint
        } childOf textComponent

        warning.hide()

        textComponent.onValueChange {

            val invalidChars = (it as String).map { char -> char }.filter { char -> !validator(char) }.toSet()

            if (invalidChars.isEmpty()) {
                changeValue(it)

                if (isWarningAppeared) {
                    warning.hide()
                    isWarningAppeared = false
                }
            } else {
                if (!isWarningAppeared) {
                    warning.unhide()
                    isWarningAppeared = true
                }

                warning.setText("Warning! You can't use these texts: (${invalidChars.joinToString(", ")})")
            }
        }
    }
}

fun TextComponent.toFilterTextComponent() = FilterTextComponent(this)