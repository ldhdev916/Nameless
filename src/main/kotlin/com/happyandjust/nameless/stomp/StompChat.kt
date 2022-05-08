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

package com.happyandjust.nameless.stomp

import java.time.LocalDateTime

sealed class StompChat {

    abstract val content: String

    abstract val at: LocalDateTime

    abstract val id: String

    class Sending(
        override val content: String,
        override val at: LocalDateTime,
        override val id: String,
        var read: Boolean = false
    ) : StompChat() {
        override fun toString(): String {
            return "Sending(content='$content', at=$at, id='$id', read=$read)"
        }
    }

    class Received(
        override val content: String,
        override val at: LocalDateTime,
        override val id: String,
        val sender: String,
        var markAsRead: Boolean = false
    ) :
        StompChat() {
        override fun toString(): String {
            return "Received(content='$content', at=$at, id='$id', sender='$sender', markAsRead=$markAsRead)"
        }
    }
}

class ObservableChatList : ArrayList<StompChat>() {

    private val observers = mutableListOf<ChatObserver>()

    fun addObserver(observer: ChatObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: ChatObserver) {
        observers.remove(observer)
    }

    override fun add(element: StompChat): Boolean {

        observers.forEach { it.onChat(element) }

        return super.add(element)
    }

    fun onRead(chat: StompChat.Sending) {
        observers.forEach { it.onRead(chat) }
    }
}

interface ChatObserver {
    fun onChat(chat: StompChat)

    fun onRead(chat: StompChat.Sending)
}