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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.MOD_ID
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.minecraftforge.fml.common.DummyModContainer
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer
import net.minecraftforge.fml.common.ModMetadata

object RemoveCertainModID : SimpleFeature(
    "removeModId",
    "Remove Certain Mod ID Sent to Server",
    enabled = true
) {

    init {
        hierarchy {
            +::mods
        }
    }

    private val modsField = Loader::class.java.getDeclaredField("mods").apply { isAccessible = true }
    private val allMods = (modsField[Loader.instance()] as List<*>).filterIsInstance<ModContainer>()

    private var mods by parameter(
        listOf(allMods.single { it.modId == MOD_ID }),
        serializer = ListSerializer(ModContainerSerializer)
    ) {
        matchKeyCategory()
        key = "mods"
        title = "Mod List"

        settings {
            listSerializer { "${it.name} ${it.version}" }
            allValueList = { allMods.sortedBy { it !in value } }
        }
    }

    @JvmStatic
    fun shouldRemoveModId(id: String): Boolean {
        return id in mods.map { it.modId }
    }

    object ModContainerSerializer : KSerializer<ModContainer> {

        private val dummyModContainer by lazy {
            val metaData = ModMetadata().apply { modId = "Dummy(Error)" }
            DummyModContainer(metaData)
        }

        override val descriptor = String.serializer().descriptor

        override fun deserialize(decoder: Decoder): ModContainer {
            val modId = decoder.decodeSerializableValue(String.serializer())

            return mods.singleOrNull { it.modId == modId } ?: dummyModContainer
        }

        override fun serialize(encoder: Encoder, value: ModContainer) {
            encoder.encodeSerializableValue(String.serializer(), value.modId)
        }
    }
}