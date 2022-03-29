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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.FeatureGui
import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.minecraft.util.ResourceLocation
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.*


object DisguiseNickname : SimpleFeature(
    "disguiseNickname",
    "Disguise Nickname",
    "Change your nickname and skin if nickname is valid!"
) {

    init {
        hierarchy {
            +::nickname

            +::changeSkin
        }
    }

    @JvmStatic
    var nickname by parameter("") {
        key = "nickname"
        title = "Disguise Nickname"
        desc = "If you leave this empty, your nickname will disappear"

        settings {
            validator = Char::isLetterOrDigit
        }
    }

    private var changeSkin by parameter(false) {
        key = "changeSkin"
        title = "Change Skin"
        desc = "If nickname you set above is valid, your skin will be changed into his skin"

        onValueChange {
            if (!it) reset()
        }
    }

    private val invalidUsernames = hashSetOf<String>()
    private var currentlyLoadedUsername: String? = null

    private var currentlyLoadedSkin: ResourceLocation? = null

    private val listener: (MinecraftProfileTexture.Type, ResourceLocation, MinecraftProfileTexture) -> Unit =
        { _, location, _ ->
            currentlyLoadedSkin = location
        }

    private fun checkAndLoadSkin(username: String) {
        if (mc.currentScreen is FeatureGui) return
        if (username in invalidUsernames) return
        if (username.equals(currentlyLoadedUsername, true)) return
        CoroutineScope(Dispatchers.IO).launch {
            currentlyLoadedUsername = username
            val uuid = getUUID(username) ?: run {
                invalidUsernames.add(username)
                currentlyLoadedUsername = null
                return@launch
            }

            val jsonObject =
                Json.decodeFromString<JsonObject>("https://sessionserver.mojang.com/session/minecraft/profile/$uuid".fetch())

            mc.skinManager.loadProfileTextures(
                GameProfile(UUID.randomUUID(), username).apply {
                    properties.put(
                        "textures",
                        Property("textures", jsonObject["properties"]!!.jsonArray[0].jsonObject["value"]!!.string, null)
                    )
                },
                listener,
                false
            )
        }
    }

    init {
        on<FeatureStateChangeEvent.Pre>().filter { feature == this@DisguiseNickname && !enabledAfter }
            .subscribe { reset() }

        on<SpecialTickEvent>().filter { enabled && changeSkin }.subscribe { checkAndLoadSkin(nickname) }
    }

    private fun reset() {
        currentlyLoadedUsername = mc.thePlayer.name
        currentlyLoadedSkin = null
    }

    @JvmStatic
    fun doChangeSkin(cir: CallbackInfoReturnable<ResourceLocation>, gameProfile: GameProfile) {
        if (gameProfile.id == mc.thePlayer?.uniqueID) {
            currentlyLoadedSkin?.let { cir.returnValue = it }
        }
    }
}