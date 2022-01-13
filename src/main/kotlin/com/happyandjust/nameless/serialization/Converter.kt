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

package com.happyandjust.nameless.serialization

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.happyandjust.nameless.dsl.fromJson

interface Converter<T> {

    fun serialize(t: T): JsonElement

    fun deserialize(jsonElement: JsonElement): T
}

class DummyConverter<T> : Converter<T> {
    override fun serialize(t: T): JsonElement {
        error("You cannot serialize DummyConverter type of ${t!!.javaClass.name}")
    }

    override fun deserialize(jsonElement: JsonElement): T {
        error("You cannot deserialize DummyConverter")
    }
}

inline fun <reified T> Gson.toConverter() = object : Converter<T> {
    override fun serialize(t: T) = toJsonTree(t)

    override fun deserialize(jsonElement: JsonElement) = fromJson<T>(jsonElement)
}