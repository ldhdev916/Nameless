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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.impl.qol.InGameStatViewer
import com.happyandjust.nameless.features.impl.settings.HypixelAPIKey
import com.happyandjust.nameless.features.order
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.DisplayName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.minecraft.util.EnumChatFormatting

object ViewStatCommand : Command("viewstat") {

    @DefaultHandler
    fun handle(@DisplayName("Name") name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val uuid = getUUID(name) ?: run {
                sendPrefixMessage("§cFailed to get uuid of $name")
                return@launch
            }

            val apiKey = HypixelAPIKey.apiKey
            if (apiKey.isBlank()) {
                sendPrefixMessage("Unknown Hypixel API Key")
                return@launch
            }

            val identifiers =
                InGameStatViewer.order.filter { it.supportGames.any(InGameStatViewer.SupportGame::shouldDisplay) }

            runCatching {
                val apiUrl = "https://api.hypixel.net/player?key=$apiKey&uuid=$uuid"
                val jsonObject = Json.decodeFromString<JsonObject>(apiUrl.fetch())["player"]!!.jsonObject

                sendClientMessage("§bStats of ${getPlayerName(jsonObject)}")

                sendClientMessage(identifiers.joinToString("\n") {
                    val type = it.informationType
                    type.getFormatText(type.getStatValue(jsonObject))
                })
            }.onFailure {
                sendClientMessage("§c$it")
                it.printStackTrace()
            }
        }
    }

    private fun getPlayerName(jsonObject: JsonObject): String {
        val displayName = jsonObject["displayname"]!!.string

        return "${getRankOfPlayer(jsonObject)}$displayName"
    }

    private fun getRankOfPlayer(jsonObject: JsonObject): String {
        val customPrefix = jsonObject["prefix"]
        if (customPrefix != null) {
            return "${customPrefix.string} "
        }

        val specialRank = jsonObject["rank"]
        if (specialRank != null) {
            when (specialRank.string) {
                "ADMIN" -> return "§c[ADMIN] "
                "MODERATOR" -> return "§2[MOD] "
                "HELPER" -> return "§9[HELPER] "
                "YOUTUBER" -> return "§c[§fYOUTUBE§c] "
            }
        }

        return when (jsonObject["newPackageRank"]?.string.orEmpty()) {
            "MVP_PLUS" -> {
                val plus = jsonObject["rankPlusColor"]?.string ?: "RED"
                val color = EnumChatFormatting.valueOf(plus)

                if (jsonObject["monthlyPackageRank"]?.string == "SUPERSTAR") {
                    "§6[MVP$color++§6] "
                } else {
                    "§b[MVP$color+§b] "
                }
            }
            "MVP" -> "§b[MVP] "
            "VIP_PLUS" -> "§a[VIP§6+§a] "
            "VIP" -> "§a[VIP] "
            else -> "§7"
        }
    }

    private fun InGameStatViewer.InformationType.getFormatText(value: String): String {
        val format = InGameStatViewer.getParameterValue<String>("texts/${name.lowercase()}_text")

        return format.replace("{value}", value).replace("&", "§")
    }
}