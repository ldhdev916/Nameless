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
import com.happyandjust.nameless.gui.feature.ColorCache
import com.ldhdev.socket.requestOnlinePlayers
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.GuiScale

class SocketGui : WindowScreen(ElementaVersion.V1, newGuiScale = GuiScale.scaleForScreenSize().ordinal) {

    init {
        Nameless.client.requestOnlinePlayers { players ->
            if (players.isEmpty()) {
                UIText("No Online Players").constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()

                    textScale = 5.pixels()
                } childOf window
            } else {

                val block = UIBlock(ColorCache.divider).constrain {

                    x = CenterConstraint()
                    y = CenterConstraint()

                    width = ChildBasedSizeConstraint()
                    height = 320.pixels()
                } childOf window

                val iconBlock = UIBlock(ColorCache.divider.darker()).constrain {
                    width = ChildBasedSizeConstraint() + 20.pixels()
                    height = 100.percent()
                } childOf block

                val iconContainer = ScrollComponent().constrain {

                    x = CenterConstraint()
                    y = CenterConstraint()

                    height = height coerceAtMost 100.percent()
                } childOf iconBlock

                val selectIcons = mutableListOf<PlayerSelectIcon>()
                val associatedChatRoom = players.associateWith { name ->
                    UIChatRoom(this, name).constrain {
                        x = SiblingConstraint(10f)

                        width = 240.pixels()
                        height = 100.percent()
                    }
                }

                players.forEachIndexed { index, name ->

                    fun associate() {
                        val chatRoom = associatedChatRoom[name]!!
                        if (block.children.indexOf(chatRoom) == -1) { // Just to be safe
                            chatRoom childOf block
                        }
                        (associatedChatRoom - name).values.forEach(block::removeChild)
                    }

                    val selected = if (selectedPlayerName != null) name == selectedPlayerName else index == 0
                    if (selected) {
                        associate()
                    }

                    val icon = PlayerSelectIcon(name, selected) {
                        selectedPlayerName = name
                        associate()
                        setSelected(true)
                        (selectIcons - this).forEach { selectIcon ->
                            selectIcon.setSelected(false)
                        }
                    }.constrain {
                        y = SiblingConstraint(10f)

                        width = 40.pixels()
                    } childOf iconContainer

                    selectIcons.add(icon)
                }
            }
        }
    }

    private val closeListeners = mutableListOf<() -> Unit>()
    fun onClose(action: () -> Unit) {
        closeListeners.add(action)
    }

    override fun onScreenClose() {
        super.onScreenClose()

        closeListeners.forEach { it() }
    }

    companion object {
        private var selectedPlayerName: String? = null
    }
}