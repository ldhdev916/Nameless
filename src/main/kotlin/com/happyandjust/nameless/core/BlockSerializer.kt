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

package com.happyandjust.nameless.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraft.block.Block

object BlockSerializer : KSerializer<Block> {

    override val descriptor = String.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Block) {
        encoder.encodeSerializableValue(String.serializer(), value.registryName)
    }

    override fun deserialize(decoder: Decoder): Block {
        val registryName = decoder.decodeSerializableValue(String.serializer())

        return Block.getBlockFromName(registryName)

    }
}