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

package com.happyandjust.nameless.config

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.serialization.Converter
import java.io.File

object ConfigHandler {
    var file = File("config/Nameless.json")
    private val handler: JsonHandler
        get() = JsonHandler(file)
    private val config: JsonObject
        get() = handler.read(JsonObject())

    private fun readCategory(category: String, config: JsonObject) =
        config[category] as? JsonObject ?: JsonObject()

    fun <T> get(category: String, key: String, defaultValue: T, converter: Converter<T>): T {
        val keyObject = (readCategory(category, config)[key] ?: return defaultValue)

        return converter.deserialize(keyObject)
    }

    fun write(category: String, key: String, write: JsonElement) {
        handler.write(config.also { it.add(category, readCategory(category, it).apply { add(key, write) }) })
    }

    fun <T> write(category: String, key: String, write: T, converter: Converter<T>) {
        write(category, key, converter.serialize(write))
    }

    fun write(category: String, key: String, number: Number) {
        write(category, key, JsonPrimitive(number))
    }

    fun write(category: String, key: String, boolean: Boolean) {
        write(category, key, JsonPrimitive(boolean))
    }

    fun write(category: String, key: String, string: String) {
        write(category, key, JsonPrimitive(string))
    }

    fun getKeys(category: String) = readCategory(category, config).entrySet().map { it.key }

    fun deleteCategory(category: String) {
        with(config) {
            remove(category)
            handler.write(this)
        }
    }

    fun deleteKey(category: String, key: String) {
        handler.write(config.also { it.add(category, readCategory(category, it).apply { remove(key) }) })
    }
}