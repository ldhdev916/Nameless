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

package com.happyandjust.nameless.serialization.converters

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.happyandjust.nameless.serialization.Converter

class CList<T>(private val serializer: (T) -> JsonElement, private val deserializer: (JsonElement) -> T) :
    Converter<List<T>> {

    override fun serialize(t: List<T>): JsonElement {
        return JsonArray().apply {
            t.map(serializer).forEach(::add)
        }
    }

    override fun deserialize(jsonElement: JsonElement): List<T> {
        return arrayListOf<T>().apply {
            addAll(jsonElement.asJsonArray.map(deserializer))
        }
    }
}