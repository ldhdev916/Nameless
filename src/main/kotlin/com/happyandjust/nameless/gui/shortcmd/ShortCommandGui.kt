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

package com.happyandjust.nameless.gui.shortcmd

import com.happyandjust.nameless.commands.ShortCommand
import com.happyandjust.nameless.gui.ActionButton
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.GuiScale

class ShortCommandGui :
    WindowScreen(newGuiScale = GuiScale.scaleForScreenSize().ordinal, restoreCurrentGuiOnClose = true) {

    init {
        val text = """
               {} means command argument
               For example, if you set short command to /sh {} and origin command to /short {}
               And if you type /sh something, it'll converted to /short something
               Command argument can be missed when not needed
           """.trimIndent()

        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = 10.percent()

            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        } childOf window

        UIText("Short Commands").constrain {
            textScale = 2.5.pixels()
        } childOf container

        ActionButton("Add New") {
            val info = ShortCommand.ShortCommandInfo("", "")

            list.add(info)

            CommandInfoComponent(this@ShortCommandGui, info) childOf scroller
        }.constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()

            width = AspectConstraint(3f)
            height = 20.pixels()
        } childOf container

        UIWrappedText(text, centered = true).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)

        } childOf window
    }

    private val commandsContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = SiblingConstraint(5f)

        width = 70.percent()
        height = 70.percent()
    } childOf window

    val scroller = ScrollComponent(
        "No short commands",
        customScissorBoundingBox = commandsContainer,
        innerPadding = 2f
    ).constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf commandsContainer

    val list = ShortCommand.shortCommandInfos.toMutableList()

    init {
        list.map { CommandInfoComponent(this, it) }.forEach {
            it childOf scroller
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()
        ShortCommand.shortCommandInfos = list
    }
}