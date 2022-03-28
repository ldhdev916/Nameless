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