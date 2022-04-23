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

import com.happyandjust.nameless.core.input.UserInputItem
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.input.AbstractTextInput
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.utils.onLeftClick

class UserInputItemComponent(
    private val userInputItem: UserInputItem,
    inputHelpers: Iterable<InputHelperComponent>
) : SettingComponent() {

    private val textHolder by UIBlock(ColorCache.darkHighlight).constrain {

        x = 0.pixels(alignOpposite = true)

        width = ChildBasedSizeConstraint() + 6.pixels()
        height = ChildBasedSizeConstraint() + 6.pixels()
    } childOf this effect OutlineEffect(ColorCache.divider, 1f)

    private val helperHolder by UIContainer().constrain {
        y = SiblingConstraint(10f)

        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf this

    init {
        inputHelpers.forEach {
            it.createComponent { text ->
                val (start, end) = getSelectionMethod(textInput) as Pair<*, *>
                val startColumn = columnField[start] as Int
                val endColumn = columnField[end] as Int

                textInput.setText(textInput.getText().replaceRange(startColumn, endColumn, text))
            }.constrain {
                y = SiblingConstraint(6f)
            } childOf helperHolder
        }
    }

    private val textInput by UITextInput("").constrain {
        x = 3.pixels()
        y = 3.pixels()
    }.setMinWidth(50.pixels()).setMaxWidth(basicWidthConstraint { parent.getWidth() * 0.5f }) childOf textHolder

    init {

        constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        textInput.onUpdate {
            changeValue(UserInputItem.parseFromPreviewString(it))
        }.onLeftClick {
            it.stopPropagation()

            textInput.grabWindowFocus()
        }.onFocus {
            textInput.setActive(true)
        }.onFocusLost {
            textInput.setActive(false)
        }
    }

    private var hasSetInitialText = false

    override fun animationFrame() {
        super.animationFrame()

        if (!hasSetInitialText) {
            textInput.setText(userInputItem.asPreviewString())
            hasSetInitialText = true
        }
    }

    companion object {
        private val getSelectionMethod by lazy {
            AbstractTextInput::class.java.getDeclaredMethod("getSelection").apply { isAccessible = true }
        }

        private val columnField by lazy {
            Class.forName("gg.essential.elementa.components.input.AbstractTextInput\$LinePosition")
                .getDeclaredField("column").apply {
                    isAccessible = true
                }
        }
    }
}