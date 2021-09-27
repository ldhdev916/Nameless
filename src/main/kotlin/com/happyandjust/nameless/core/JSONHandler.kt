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

package com.happyandjust.nameless.core

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.happyandjust.nameless.devqol.mc
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.InputStream

class JSONHandler(inputStream: InputStream? = null) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val parser = JsonParser()
    private var jsonData: JsonElement? = null

    /**
     * If I call file.outputStream in init, file will be reset
     * So only call file.outputStream when writing
     */
    private var file: File? = null

    constructor(resourceLocation: ResourceLocation) : this(
        mc.mcDefaultResourcePack.getInputStream(resourceLocation).buffered()
    )

    constructor(file: File) : this(if (file.isFile) file.inputStream().buffered() else null) {
        this.file = file
    }

    constructor(jsonString: String) : this() {
        jsonData = try {
            parser.parse(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    init {
        inputStream?.let {
            jsonData = try {
                parser.parse(it.bufferedReader())
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    }

    fun <T : JsonElement> read(defaultValue: T): T = jsonData?.let {
        try {
            gson.fromJson(it, defaultValue::class.java)
        } catch (e: Exception) {
            defaultValue
        }
    } ?: defaultValue

    fun write(write: JsonElement): JSONHandler {

        file?.bufferedWriter()?.use {
            gson.toJson(write, it)
            it.flush()
        }

        return this
    }

}
