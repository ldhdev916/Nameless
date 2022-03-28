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

package com.happyandjust.nameless.config

import com.happyandjust.nameless.dsl.decodeFromFile
import com.happyandjust.nameless.dsl.encodeToFile
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import java.io.File
import kotlin.collections.set

object ConfigHandler {
    private val customJson = Json { prettyPrint = true }
    var file = File("config/Nameless.json").apply {
        if (!exists() || readBytes().decodeToString().isBlank()) {
            parentFile.mkdirs()
            writeText("{}")
        }
    }

    private val config: JsonObject
        get() = customJson.decodeFromFile(file)

    private fun Map<String, JsonElement>.readCategory(category: String) =
        get(category) as? JsonObject ?: JsonObject(emptyMap())

    fun <T> get(category: String, key: String, defaultValue: T, serializer: KSerializer<T>): T {
        val keyObject = (config.readCategory(category)[key] ?: return defaultValue)

        return customJson.decodeFromJsonElement(serializer, keyObject)
    }

    fun <T> get(category: String, key: String, serializer: KSerializer<T>): T {
        val keyObject = config.readCategory(category)[key]!!

        return customJson.decodeFromJsonElement(serializer, keyObject)
    }

    inline fun <reified T> get(category: String, key: String, defaultValue: T) =
        get(category, key, defaultValue, serializer())

    inline fun <reified T> get(category: String, key: String): T = get(category, key, serializer())

    fun write(category: String, key: String, write: JsonElement) {
        val writeMap = config.toMutableMap()
        val existing = writeMap.readCategory(category).toMutableMap().apply { put(key, write) }
        writeMap[category] = JsonObject(existing)
        customJson.encodeToFile(writeMap.toMap(), file)
    }

    fun <T> write(category: String, key: String, write: T, serializer: KSerializer<T>) {
        write(category, key, customJson.encodeToJsonElement(serializer, write))
    }

    inline fun <reified T> write(category: String, key: String, write: T) = write(category, key, write, serializer())

    fun getKeys(category: String) = config.readCategory(category).keys

    fun deleteCategory(category: String) {
        with(config.toMutableMap()) {
            remove(category)
            customJson.encodeToFile(toMap(), file)
        }
    }

    fun deleteKey(category: String, key: String) {
        val writeMap = config.toMutableMap()
        val existing = writeMap.readCategory(category).toMutableMap().apply { remove(key) }
        writeMap[category] = JsonObject(existing)

        customJson.encodeToFile(writeMap.toMap(), file)
    }
}