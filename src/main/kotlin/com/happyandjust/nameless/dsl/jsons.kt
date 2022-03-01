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

package com.happyandjust.nameless.dsl

import gg.essential.api.utils.WebUtil
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.int
import net.minecraft.util.ResourceLocation
import java.io.File
import java.io.InputStream

fun String.fetch() = WebUtil.fetchString(this)!!

fun ResourceLocation.inputStream(): InputStream = mc.mcDefaultResourcePack.getInputStream(this)

val JsonElement.string
    get() = jsonPrimitive.content

val JsonElement.int
    get() = jsonPrimitive.int

val JsonElement.boolean
    get() = jsonPrimitive.boolean

val JsonElement.double: Double
    get() = jsonPrimitive.double

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Json.encodeToFile(value: T, file: File) = file.outputStream().buffered().use {
    encodeToStream(value, it)
    it.flush()
}

@OptIn(ExperimentalSerializationApi::class)
inline fun <reified T> Json.decodeFromFile(file: File): T = file.inputStream().buffered().use(this::decodeFromStream)

inline fun <reified T : Any> dummySerializer() = object : KSerializer<T> {
    override val descriptor: SerialDescriptor
        get() = error("You can not serialize ${T::class.java.name}")

    override fun serialize(encoder: Encoder, value: T) {
        error("You can not serialize ${T::class.java.name}")
    }

    override fun deserialize(decoder: Decoder): T {
        error("You can not deserialize ${T::class.java.name}")
    }
}