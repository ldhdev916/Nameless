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

package com.happyandjust.nameless.gui.socket

import com.happyandjust.nameless.Nameless
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.utils.onLeftClick
import org.lwjgl.input.Keyboard

class UIChatRoom(gui: SocketGui, with: String) : UIContainer() {

    init {

        val topContainer = UIContainer().constrain {
            width = 100.percent()
            height = ChildBasedSizeConstraint() + 20.pixels()
        } childOf this

        UIText(with).constrain {

            x = CenterConstraint()
            y = CenterConstraint()

            textScale = 2.pixels()
        } childOf topContainer

        ChatContainer(gui, Nameless.client.getOrCreateChats(with)).constrain {

            y = SiblingConstraint()

            width = 100.percent()
            height = FillConstraint()
        } childOf this

        val bottomContainer = UIContainer().constrain {
            y = 0.pixel(alignOpposite = true)

            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        } childOf this

        val input = UITextInput("Message").constrain {

            y = CenterConstraint()

            width = 80.percent()
        } childOf bottomContainer

        val send = send@{
            val text = input.getText().ifBlank { return@send }
            Nameless.client.sendChat(with, text)
            input.setText("")
        }

        input.onLeftClick {
            grabWindowFocus()
        }.onKeyType { _, keyCode ->
            if (keyCode == Keyboard.KEY_RETURN) {
                send()
            }
        }

        SendButton(send).constrain {
            x = SiblingConstraint()
            y = CenterConstraint()

            width = FillConstraint(useSiblings = false)
            height = AspectConstraint(0.5f)
        } childOf bottomContainer
    }
}