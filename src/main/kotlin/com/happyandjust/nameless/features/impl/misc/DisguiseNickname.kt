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
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.changeSkin
import com.happyandjust.nameless.features.nickname
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.FeatureGui
import com.happyandjust.nameless.mixins.accessors.AccessorAbstractClientPlayer
import com.happyandjust.nameless.mixins.accessors.AccessorNetworkPlayerInfo
import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.minecraft.util.ResourceLocation
import java.util.*


object DisguiseNickname : SimpleFeature(
    "disguiseNickname",
    "Disguise Nickname",
    "Change your nickname and skin if nickname is valid!"
) {

    @JvmStatic
    val enabledJVM
        get() = enabled

    @JvmStatic
    var nicknameJVM
        get() = nickname
        set(value) {
            nickname = value
        }

    init {
        parameter("") {
            matchKeyCategory()
            key = "nickname"
            title = "Disguise Nickname"
            desc = "If you leave this empty, your nickname will disappear"

            settings {
                validator = Char::isLetterOrDigit
            }
        }

        parameter(false) {
            matchKeyCategory()
            key = "changeSkin"
            title = "Change Skin"
            desc = "If nickname you set above is valid, your skin will be changed into his skin"

            onValueChange {
                if (!it) resetTexture()
            }

            settings {
                ordinal = 1
            }
        }
    }

    private val invalidUsernames = hashSetOf<String>()
    private var currentlyLoadedUsername: String? = null
    private val listener: (MinecraftProfileTexture.Type, ResourceLocation, MinecraftProfileTexture) -> Unit =
        { _, location, _ ->
            ((mc.thePlayer as AccessorAbstractClientPlayer).invokeGetPlayerInfo() as AccessorNetworkPlayerInfo)
                .setLocationSkin(location)
        }

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkAndLoadSkin(username: String) {
        if (mc.currentScreen is FeatureGui) return
        if (username in invalidUsernames) return
        if (username.equals(currentlyLoadedUsername, true)) return
        GlobalScope.launch {
            currentlyLoadedUsername = username
            val uuid = username.getUUID() ?: run {
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
            .subscribe { resetTexture() }

        on<SpecialTickEvent>().filter { enabled && changeSkin }.subscribe { checkAndLoadSkin(nickname) }
    }

    private fun resetTexture() {
        currentlyLoadedUsername = mc.thePlayer.name
        mc.thePlayer.withInstance<AccessorAbstractClientPlayer> {
            invokeGetPlayerInfo().withInstance<AccessorNetworkPlayerInfo> {
                setLocationSkin(null)
                setPlayerTexturesLoaded(false)
            }
        }
    }
}