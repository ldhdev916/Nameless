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
import com.happyandjust.nameless.stomp.ChatObserver
import com.happyandjust.nameless.stomp.ObservableChatList
import com.happyandjust.nameless.stomp.StompChat
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import java.awt.Color
import java.time.format.DateTimeFormatter

class ChatContainer(chats: ObservableChatList) : UIContainer() {

    init {
        chats.filterIsInstance<StompChat.Received>().forEach { Nameless.client.markChatAsRead(it) }

        val scrollBar = UIBlock(Color.white.withAlpha(0.15f)).constrain {
            x = 100.percent() - 3.pixels()
            width = 3.pixels()
        } childOf this

        val scroller = ScrollComponent("No chatting available").constrain {
            width = 100.percent()
            height = 100.percent()
        }.apply {
            scrollToBottom()
            setVerticalScrollBarComponent(scrollBar, hideWhenUseless = true)
        } childOf this

        chats.forEach {
            StompChatContainer(it) childOf scroller
        }

        val observer = object : ChatObserver {
            override fun onChat(chat: StompChat) {
                StompChatContainer(chat) childOf scroller

                scroller.scrollToBottom()
                if (chat is StompChat.Received) {
                    Nameless.client.markChatAsRead(chat)
                }
            }

            override fun onRead(chat: StompChat.Sending) {
                scroller.allChildren.filterIsInstance<StompChatContainer>().find { it.chat == chat }?.markAsRead()
            }
        }

        chats.addObserver(observer)
    }
}

private class StompChatContainer(val chat: StompChat) : UIContainer() {
    init {
        constrain {
            y = SiblingConstraint(10f)

            width = 100.percent()
            height = ChildBasedSizeConstraint()
        }
    }

    private val chatBox by ChatBox(chat) childOf this

    fun markAsRead() {
        chatBox.markAsRead()
    }
}

private class ChatBox(chat: StompChat) : UIContainer() {
    fun markAsRead() {
        val child = children.find { it is UIText && it.getText() == "1" } ?: return
        removeChild(child)
    }

    init {
        constrain {
            x = 8.pixel(alignOpposite = chat is StompChat.Sending)

            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        }

        val formatter = DateTimeFormatter.ofPattern("a h:mm")
        val timeText = UIText(chat.at.format(formatter)).constrain {
            if (chat is StompChat.Received) {
                x = SiblingConstraint(3f)
            }
            y = 0.pixel(alignOpposite = true)
        }

        if (chat is StompChat.Sending) {
            if (!chat.read) {
                UIText("1").constrain {
                    color = Color.yellow.constraint
                    textScale = 0.7.pixels()
                } childOf this
            }
            timeText childOf this
        }

        val container = UIRoundedRectangle(4f).constrain {
            if (chat is StompChat.Sending) {
                x = SiblingConstraint(3f)
            }
            color = if (chat is StompChat.Received) Color.white.constraint else Color.yellow.constraint

            width = ChildBasedSizeConstraint() + 10.pixels()
            height = ChildBasedSizeConstraint() + 10.pixels()
        } childOf this

        if (chat is StompChat.Received) {
            timeText childOf this
        }

        UIWrappedText(chat.content, shadow = false).constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            width = width coerceAtLeast 6.pixels() coerceAtMost 50.pixels()
            color = Color.black.constraint
        } childOf container
    }
}