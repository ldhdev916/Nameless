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

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.serialization.Converter
import java.io.File

object ConfigHandler {
    private val file = File("config/Nameless.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val handler: JSONHandler
        get() = JSONHandler(file)

    private fun readConfig() = handler.read(JsonObject())

    private fun readCategory(category: String, config: JsonObject = readConfig()) =
        config[category] as? JsonObject ?: JsonObject()


    fun <T> get(category: String, key: String, defaultValue: T, converter: Converter<T>): T {
        val keyObject = (readCategory(category)[key] ?: return defaultValue)

        return converter.deserialize(keyObject)
    }

    fun write(category: String, key: String, write: JsonElement) {
        val configObject = readConfig()

        val categoryObject = readCategory(category, configObject)

        categoryObject.add(key, write)

        configObject.add(category, categoryObject)

        handler.write(configObject)
    }

    fun <T> write(category: String, key: String, write: T, converter: Converter<T>) {
        write(category, key, converter.serialize(write))
    }

    fun write(category: String, key: String, int: Int) {
        write(category, key, JsonPrimitive(int))
    }

    fun write(category: String, key: String, double: Double) {
        write(category, key, JsonPrimitive(double))
    }

    fun write(category: String, key: String, boolean: Boolean) {
        write(category, key, JsonPrimitive(boolean))
    }

    fun write(category: String, key: String, string: String) {
        write(category, key, JsonPrimitive(string))
    }

    fun getKeys(category: String): Iterable<String> {

        val jsonObject = readCategory(category)

        return arrayListOf<String>().apply {
            for ((s, _) in jsonObject.entrySet()) {
                add(s)
            }
        }
    }

    fun deleteCategory(category: String) {
        handler.write(readConfig().remove(category))
    }

    fun deleteKey(category: String, key: String) {

        val configObject = readConfig()

        val categoryObject = readCategory(category, configObject)
        configObject.add(category, categoryObject.remove(key))

        handler.write(configObject)
    }
}
