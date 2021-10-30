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
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.devqol.inHypixel
import com.happyandjust.nameless.devqol.matchesMatcher
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.PacketListener
import com.happyandjust.nameless.mixins.accessors.AccessorGuiChat
import com.happyandjust.nameless.network.Request
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.server.S3APacketTabComplete
import java.util.regex.Pattern

object FeaturePlayTabComplete : SimpleFeature(
    Category.QOL,
    "playtabcomplete",
    "/play Auto Tab Complete",
    "Automatically gets all hypixel games when you press tab with /play",
    true
), PacketListener {

    private val gameMap = hashMapOf<String, String>()
    private val games = arrayListOf<String>()

    /**
     * Thanks asbyth
     */
    fun fetchGameDataList() {
        val json =
            JSONHandler(Request.get("https://gist.githubusercontent.com/asbyth/16ab6fcbca18f3f4a14d61d04e7ebeb5/raw")).read(
                JsonObject()
            )

        for ((s, element) in json.entrySet()) {
            gameMap[s] = element.asString

            games.add(s)
            games.add(element.asString)
        }
    }

    private val PLAY = Pattern.compile("/play (?<msg>.*)")

    override fun onSendingPacket(e: PacketEvent.Sending) {
        if (!mc.thePlayer.inHypixel() || !enabled) return

        val msg = e.packet

        if (msg is C01PacketChatMessage) {
            PLAY.matchesMatcher(msg.message) {
                val game = it.group("msg")

                e.packet = C01PacketChatMessage("/play ${gameMap[game] ?: game}")

            }
        }
    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
        if (!mc.thePlayer.inHypixel() || !enabled) return
        val gui = mc.currentScreen
        if (gui !is AccessorGuiChat) return
        val msg = e.packet

        if (msg is S3APacketTabComplete) {
            PLAY.matchesMatcher(gui.inputField.text) {
                val game = it.group("msg")
                e.packet =
                    S3APacketTabComplete(games.filter { map -> map.contains(game, true) }.toTypedArray())
            }
        }
    }
}
