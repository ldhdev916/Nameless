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

package com.happyandjust.nameless.gui.shortcmd

import com.happyandjust.nameless.commands.ShortCommand
import com.happyandjust.nameless.gui.RemoveButton
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.utils.onLeftClick

class CommandInfoComponent(
    private val parentGui: ShortCommandGui,
    private val shortCommandInfo: ShortCommand.ShortCommandInfo
) : UIContainer() {

    init {

        constrain {
            y = SiblingConstraint(10f)

            width = 100.percent() - 10.pixels()
            height = ChildBasedMaxSizeConstraint() + 10.pixels()
        }

        effect(OutlineEffect(ColorCache.accent, 1f))

        val inputWidth = 50.percent() - 60.pixels()

        UITextInput("Shortened").apply {
            setText(shortCommandInfo.short)

            onFocusLost {
                shortCommandInfo.short = getText()
            }
        }.constrain {
            y = CenterConstraint()

            width = inputWidth
        }.onLeftClick {
            grabWindowFocus()
        } childOf this

        UITextInput("Origin").apply {
            setText(shortCommandInfo.origin)

            onFocusLost {
                shortCommandInfo.origin = getText()
            }
        }.constrain {
            x = 30.pixel(true)
            y = CenterConstraint()

            width = inputWidth

        }.onLeftClick {
            grabWindowFocus()
        } childOf this

        RemoveButton {
            parentGui.list.remove(shortCommandInfo)
            parentGui.scroller.removeChild(this)
        }.constrain {
            x = 10.pixels(true)
            y = CenterConstraint()
        } childOf this

    }
}