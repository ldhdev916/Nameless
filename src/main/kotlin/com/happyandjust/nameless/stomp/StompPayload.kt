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

import java.util.*

class StompPayload(val method: StompHeader = StompHeader.SEND) {
    val headers = mutableMapOf<String, String>()
    var payload: String? = null
        private set

    fun header(vararg header: Pair<String, Any?>) = apply {
        headers.putAll(header.map { (k, v) -> k to v.toString() })
    }

    fun payload(s: String?) = apply {
        payload = s
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
        return "StompPayload(method=$method, headers=$headers, payload=$payload)"
    }


    companion object {
        fun parse(s: String): StompPayload {
            val sc = Scanner(s)
            val method = StompHeader.valueOf(sc.nextLine())
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

            return StompPayload(method).header(*headers.toList().toTypedArray()).payload(payload.ifEmpty { null })
        }
    }
}