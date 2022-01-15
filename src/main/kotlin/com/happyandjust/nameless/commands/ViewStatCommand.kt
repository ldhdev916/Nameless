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

package com.happyandjust.nameless.commands

import com.google.gson.JsonObject
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.impl.qol.InGameStatViewer
import com.happyandjust.nameless.features.impl.settings.HypixelAPIKey
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.DisplayName
import gg.essential.api.utils.Multithreading
import kotlinx.coroutines.DelicateCoroutinesApi
import net.minecraft.util.EnumChatFormatting

object ViewStatCommand : Command("viewstat") {

    @OptIn(DelicateCoroutinesApi::class)
    @DefaultHandler
    fun handle(@DisplayName("Name") name: String) {
        Multithreading.runAsync {
            val uuid = name.getUUID() ?: run {
                sendPrefixMessage("§cFailed to get uuid of $name")
                return@runAsync
            }

            val identifiers = InGameStatViewer.order.filter { it.supportGame.shouldDisplay() }

            runCatching {
                val handler = "https://api.hypixel.net/player?key=${HypixelAPIKey.apiKey}&uuid=$uuid".handler()
                val json = handler.read<JsonObject>()["player"].asJsonObject

                sendClientMessage("§bStats of ${getPlayerName(json)}")

                sendClientMessage(identifiers.joinToString("\n") {
                    it.informationType.run { getFormatText(getStatValue(json)) }
                })
            }.onFailure { it.notifyException() }
        }
    }

    private fun getPlayerName(jsonObject: JsonObject): String {
        val displayName = jsonObject["displayname"].asString

        return "${processRank(jsonObject)}$displayName"
    }

    private fun processRank(jsonObject: JsonObject): String {
        if (jsonObject.has("prefix")) { // WTF
            return "${jsonObject["prefix"].asString} "
        }

        if (jsonObject.has("rank")) {
            when (jsonObject["rank"].asString) {
                "ADMIN" -> return "§c[ADMIN] "
                "MODERATOR" -> return "§2[MOD] "
                "HELPER" -> return "§9[HELPER] "
                "YOUTUBER" -> return "§c[§fYOUTUBE§c] "
            }
        }

        return when (runCatching { jsonObject["newPackageRank"].asString }.getOrDefault("NONE")) {
            "MVP_PLUS" -> {
                val plus = if (jsonObject.has("rankPlusColor")) jsonObject["rankPlusColor"].asString else "RED"
                val color = EnumChatFormatting.valueOf(plus)

                if (jsonObject["monthlyPackageRank"].asString == "SUPERSTAR") {
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

    private fun InGameStatViewer.InformationType.getFormatText(
        value: String
    ): String {
        val format = InGameStatViewer.getParameter<String>("texts").getParameterValue<String>(name.lowercase())

        return format.replace("{value}", value).replace("&", "§")
    }
}