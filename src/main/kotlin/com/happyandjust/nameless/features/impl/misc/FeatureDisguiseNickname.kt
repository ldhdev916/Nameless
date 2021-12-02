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

package com.happyandjust.nameless.features.impl.misc

import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.core.Request
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.FeatureStateListener
import com.happyandjust.nameless.gui.feature.FeatureGui
import com.happyandjust.nameless.mixins.accessors.AccessorAbstractClientPlayer
import com.happyandjust.nameless.mixins.accessors.AccessorNetworkPlayerInfo
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CString
import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import com.mojang.authlib.properties.Property
import gg.essential.api.EssentialAPI
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.util.ResourceLocation
import java.util.*


object FeatureDisguiseNickname : SimpleFeature(
    Category.MISCELLANEOUS,
    "disguisenickname",
    "Disguise Nickname",
    "Change your nickname and skin if nickname is valid!"
), FeatureStateListener, ClientTickListener {

    init {
        parameters["nick"] = FeatureParameter(
            0,
            "disguise",
            "nickname",
            "Disguise Nickname",
            "If you leave this empty, your nickname will disappear",
            "",
            CString
        ).apply {
            validator = { char -> char.isLetterOrDigit() }
        }
        parameters["skin"] = FeatureParameter(
            1,
            "disguise",
            "chnageskin",
            "Change Skin",
            "If nickname you set above is valid, your skin will be changed into his skin",
            false,
            CBoolean
        ).apply {
            onValueChange = { value ->
                if (!value) resetTexture()
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

    fun getNickname() = getParameterValue<String>("nick")

    @OptIn(DelicateCoroutinesApi::class)
    private fun checkAndLoadSkin(username: String) {
        if (mc.currentScreen is FeatureGui) return // in case you're writing username but mod stupidly gets all text you write
        if (invalidUsernames.contains(username)) return
        if (username.equals(currentlyLoadedUsername, true)) return
        GlobalScope.launch {
            currentlyLoadedUsername = username
            val uuid = EssentialAPI.getMojangAPI().getUUID(username)?.get() ?: run {
                invalidUsernames.add(username)
                currentlyLoadedUsername = null
                return@launch
            }

            val json =
                JsonHandler(Request.get("https://sessionserver.mojang.com/session/minecraft/profile/$uuid"))
                    .read(JsonObject())

            mc.skinManager.loadProfileTextures(
                GameProfile(UUID.randomUUID(), username).apply {
                    properties.put(
                        "textures",
                        Property("textures", json["properties"].asJsonArray[0].asJsonObject["value"].asString, null)
                    )
                },
                listener,
                false
            )
        }
    }

    override fun onFeatureStateChangePre(e: FeatureStateChangeEvent.Pre) {
        if (e.feature == this && !e.enabledAfter) {
            resetTexture()
        }
    }

    private fun resetTexture() {
        with(((mc.thePlayer as AccessorAbstractClientPlayer).invokeGetPlayerInfo() as AccessorNetworkPlayerInfo)) {
            setLocationSkin(null)
            setPlayerTexturesLoaded(false)
        }
        currentlyLoadedUsername = mc.thePlayer.name
    }

    override fun onFeatureStateChangePost(e: FeatureStateChangeEvent.Post) {

    }

    override fun tick() {
        if (enabled && getParameterValue("skin")) {
            checkAndLoadSkin(getParameterValue("nick"))
        }
    }
}