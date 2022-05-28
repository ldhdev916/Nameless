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

package com.ldhdev.socket.data

import com.ldhdev.namelessstd.*
import com.ldhdev.socket.chat.StompChat
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

open class StompPayload(val method: StompMethod, val payload: String? = null) {
    val headers = mutableMapOf<String, String>()

    fun header(vararg provided: Pair<String, Any?>) = apply {
        headers.putAll(provided.map { (k, v) -> k to v.toString() })
    }

    fun getMessage() = buildString {
        append(method.name)
        append("\n")
        headers.forEach { (key, value) ->
            check(":" !in key) { "Key contains : character" }
            check(":" !in value) { "Value contains : character" }

            append("$key:$value\n")
        }
        append("\n")
        payload?.let {
            append(it)
        }
        append(0.toChar())
    }

    override fun toString(): String {
        return "StompPayload(method=$method, payload=$payload, headers=$headers)"
    }


    companion object {
        fun parse(s: String): StompPayload {
            val sc = Scanner(s)
            val method = StompMethod.valueOf(sc.nextLine())
            val headers = buildMap {
                var line: String
                while (sc.nextLine().also { line = it }.isNotEmpty()) {
                    val split = line.split(":")
                    check(split.size == 2) { "Illegal header: $line" }
                    put(split[0], split[1])
                }
            }
            val payload = buildList {
                while (sc.hasNextLine()) {
                    add(sc.nextLine())
                }
            }.joinToString("\n").substringBeforeLast(0.toChar())

            return StompPayload(method, payload.ifEmpty { null }).header(*headers.toList().toTypedArray())
        }
    }
}

open class StompSend(destination: String, payload: String? = null) : StompPayload(StompMethod.SEND, payload) {
    init {
        header("destination" to destination.withPrefix(Prefix.Server))
    }
}

class StompSendChat(chat: StompChat.Sending) :
    StompSend(Route.Server.SendChat.withVariables(Variable.To to chat.receiver), chat.data.content) {

    init {
        header(Headers.ChatId to chat.data.id)
    }
}

class StompMarkAsRead(chat: StompChat.Received) :
    StompSend(Route.Server.ReadChat.withVariables(Variable.Sender to chat.sender)) {
    init {
        header(Headers.ChatId to chat.data.id)
    }
}

class StompLocrawInfo(locrawInfo: LocrawInfo?) : StompSend(Route.Server.Locraw, Json.encodeToString(locrawInfo))