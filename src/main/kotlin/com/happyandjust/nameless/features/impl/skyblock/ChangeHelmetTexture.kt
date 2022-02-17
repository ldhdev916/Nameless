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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.config.configValue
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import java.util.*

object ChangeHelmetTexture : SimpleFeature(
    "changeHelmetTexture",
    "Change Helmet Texture",
    "Change your current helmet. only works if you're wearing skull in SkyBlock. To select helmet texture, type /helmettexture [SkyBlock ID]"
) {

    @JvmStatic
    val enabledJVM
        get() = enabled
    private val currentlyEquipedTextureConfig = configValue("helmetTexture", "current", SkyBlockItem("", "", "", ""))

    @JvmStatic
    var currentlyEquipedTexture: Pair<SkyBlockItem, GameProfile>? = null
        get() = if (Hypixel.currentGame != GameType.SKYBLOCK) null else field
        set(value) {
            field = value

            value?.let {
                currentlyEquipedTextureConfig.value = it.first
            }
        }

    init {
        currentlyEquipedTextureConfig.value.takeIf { it.skin.isNotBlank() }?.let {
            setCurrentHelmetTexture(it)
        }
    }


    fun setCurrentHelmetTexture(skyBlockItem: SkyBlockItem) {
        currentlyEquipedTexture = skyBlockItem to GameProfile(UUID.randomUUID(), "CustomHelmetTexture").also {
            it.properties.put("textures", Property("textures", skyBlockItem.skin, null))
        }
    }
}