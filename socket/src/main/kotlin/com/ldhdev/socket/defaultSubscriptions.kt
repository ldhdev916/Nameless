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

package com.ldhdev.socket

import com.ldhdev.ksp.DefaultSubscription
import com.ldhdev.socket.chat.StompChat
import com.ldhdev.socket.chat.StompChatData
import com.ldhdev.socket.data.StompSend
import com.ldhdev.socket.subscription.StompMessageHandler
import java.time.LocalDateTime

@DefaultSubscription("/chats/send", DefaultSubscription.Kind.USER)
val chatHandler = StompMessageHandler {
    val sender = it.headers["sender"]!!
    val chatId = it.headers["chat-id"]!!

    val chatData = StompChatData(it.payload!!, LocalDateTime.now(), chatId)
    val chat = StompChat.Received(chatData, sender)

    getOrCreateChats(sender).add(chat)
}

@DefaultSubscription("/position", DefaultSubscription.Kind.USER)
val positionHandler = StompMessageHandler {
    withListener<StompListener.OnPosition> {
        val message = getPosition().toString()

        send(StompSend("/position", message))
    }
}

@DefaultSubscription("/chats/read", DefaultSubscription.Kind.USER)
val readHandler = StompMessageHandler {
    val reader = it.headers["reader"]!!
    val chatId = it.headers["chat-id"]!!
    val list = chats[reader] ?: return@StompMessageHandler
    val chat = list.filterIsInstance<StompChat.Sending>().first { sending -> sending.data.id == chatId }
    chat.read = true
    withListener<StompListener.OnRead> { onRead(chat) }
}