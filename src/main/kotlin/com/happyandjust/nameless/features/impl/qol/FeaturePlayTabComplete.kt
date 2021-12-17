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

package com.happyandjust.nameless.features.impl.qol

import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.core.Request
import com.happyandjust.nameless.dsl.matchesMatcher
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.mixins.accessors.AccessorGuiChat
import gg.essential.api.EssentialAPI
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S3APacketTabComplete

object FeaturePlayTabComplete : SimpleFeature(
    Category.QOL,
    "playtabcomplete",
    "/play Auto Tab Complete",
    "Automatically gets all hypixel games when you press tab with /play",
    true
) {

    private val gameMap = hashMapOf<String, String>()
    private val games = arrayListOf<String>()

    fun fetchGameDataList() {
        val json =
            JsonHandler(Request.get("https://gist.githubusercontent.com/asbyth/16ab6fcbca18f3f4a14d61d04e7ebeb5/raw")).read(
                JsonObject()
            )

        gameMap.putAll(json.entrySet().map { it.key to it.value.asString })
        games.addAll(gameMap.keys + gameMap.values)
    }

    private val PLAY = "/play (?<msg>.*)".toPattern()

    init {
        on<PacketEvent.Sending>().filter { EssentialAPI.getMinecraftUtil().isHypixel() && enabled }.subscribe {
            packet.withInstance<C01PacketChatMessage> {
                PLAY.matchesMatcher(message) {
                    val game = it.group("msg")
                    packet = C01PacketChatMessage("/play ${gameMap[game] ?: game}")
                }
            }
        }

        on<PacketEvent.Received>().filter {
            EssentialAPI.getMinecraftUtil().isHypixel() && enabled && packet is S3APacketTabComplete
        }.subscribe {
            mc.currentScreen.withInstance<AccessorGuiChat> {
                PLAY.matchesMatcher(inputField.text) {
                    val game = it.group("msg")
                    packet = S3APacketTabComplete(games.filter { map -> map.contains(game, true) }.toTypedArray())
                }
            }
        }
    }

}