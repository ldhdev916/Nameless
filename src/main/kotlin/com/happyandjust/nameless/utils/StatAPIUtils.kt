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

package com.happyandjust.nameless.utils

import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.features.impl.qol.FeatureInGameStatViewer
import com.happyandjust.nameless.features.impl.settings.FeatureHypixelAPIKey
import com.happyandjust.nameless.network.Request
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.math.sqrt

object StatAPIUtils {

    private val playerJSONCache = hashMapOf<EntityPlayer, JsonObject>()
    private val processingRequest = hashSetOf<EntityPlayer>()

    private val threadPool = Executors.newFixedThreadPool(4)

    val networkExpToLevel: (Double) -> Int = { 1 + ((-8750 + sqrt(8750.0.pow(2) + 5000 * it)) / 2500).toInt() }
    val skyWarsExpToLevel: (Int) -> Int = {
        when (it) {
            in 0 until 20 -> 1
            in 20 until 70 -> 2
            in 70 until 150 -> 3
            in 150 until 250 -> 4
            in 250 until 500 -> 5
            in 500 until 1000 -> 6
            in 1000 until 2000 -> 7
            in 2000 until 3500 -> 8
            in 3500 until 6000 -> 9
            in 6000 until 10000 -> 10
            in 10000 until 15000 -> 11
            else -> {
                val rest = it - 15000

                12 + (rest / 10000)
            }
        }
    }

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onServerChange(e: HypixelServerChangeEvent) {
        playerJSONCache.clear()
        processingRequest.clear()
    }

    fun getStatValue(player: EntityPlayer, informationType: FeatureInGameStatViewer.InformationType): String {

        val json = playerJSONCache[player] ?: run {

            if (processingRequest.add(player)) {

                threadPool.submit {
                    val api = FeatureHypixelAPIKey.apiKey

                    val uuid = player.uniqueID
                    if (uuid.version() != 4) {
                        return@submit
                    }

                    val s = Request.get("https://api.hypixel.net/player?key=$api&uuid=$uuid")

                    playerJSONCache[player] = JSONHandler(s).read(JsonObject())["player"].asJsonObject

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