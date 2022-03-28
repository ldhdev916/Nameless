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

package com.happyandjust.nameless.utils

import com.happyandjust.nameless.dsl.fetch
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.features.impl.qol.InGameStatViewer
import com.happyandjust.nameless.features.impl.settings.HypixelAPIKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.minecraft.entity.player.EntityPlayer

object StatAPIUtils {

    private val playerJSONCache = hashMapOf<EntityPlayer, JsonObject>()
    private val processingRequest = hashSetOf<EntityPlayer>()

    init {
        on<HypixelServerChangeEvent>().subscribe {
            playerJSONCache.clear()
            processingRequest.clear()
        }
    }

    fun getStatValue(player: EntityPlayer, informationType: InGameStatViewer.InformationType): String {

        val json = playerJSONCache[player] ?: run {
            if (processingRequest.add(player)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val api = HypixelAPIKey.apiKey

                    val uuid = player.uniqueID
                    if (uuid.version() != 4) {
                        return@launch
                    }
                    playerJSONCache[player] =
                        Json.decodeFromString<JsonObject>("https://api.hypixel.net/player?key=$api&uuid=$uuid".fetch())["player"]!!.jsonObject

                    processingRequest.remove(player)
                }
            }

            return "§e???"
        }

        return try {
            informationType.getStatValue(json)
        } catch (e: Exception) {
            "§c???"
        }
    }
}