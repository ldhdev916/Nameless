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

package com.ldhdev.socket.chat

sealed interface StompChat {

    val data: StompChatData

    class Sending(override val data: StompChatData, val receiver: String) : StompChat {
        var read = false

        override fun toString(): String {
            return "Sending(data=$data, receiver='$receiver', read=$read)"
        }

    }

    class Received(override val data: StompChatData, val sender: String) : StompChat {
        var markedAsRead = false

        override fun toString(): String {
            return "Received(data=$data, sender='$sender', markedAsRead=$markedAsRead)"
        }
    }
}
