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

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.happyandjust.nameless.dsl.fromJson
import com.happyandjust.nameless.dsl.globalGson
import com.happyandjust.nameless.dsl.mc
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class JsonHandler(inputStream: InputStream? = null, val outputStream: () -> OutputStream? = { null }) {
    private val parser = JsonParser()
    var jsonData: JsonElement? = null

    constructor(resourceLocation: ResourceLocation) : this(
        mc.mcDefaultResourcePack.getInputStream(resourceLocation).buffered()
    )

    constructor(resourceDomain: String, resourcePath: String) : this(ResourceLocation(resourceDomain, resourcePath))

    constructor(file: File) : this(if (file.isFile) file.inputStream().buffered() else null, { file.outputStream() })

    constructor(jsonString: String?) : this() {
        jsonData = runCatching { parser.parse(jsonString) }.getOrNull()
    }

    init {
        inputStream?.run {
            jsonData = runCatching { parser.parse(bufferedReader()) }.getOrNull()
        }
    }

    inline fun <reified T> read(): T = globalGson.fromJson(jsonData!!)

    fun write(write: Any) = apply {
        outputStream()?.bufferedWriter()?.use {
            globalGson.toJson(write, it)
            it.flush()
        }
    }

}