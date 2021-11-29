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
import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.core.Request
import com.happyandjust.nameless.dsl.notifyException
import com.happyandjust.nameless.dsl.sendClientMessage
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.features.impl.qol.FeatureInGameStatViewer
import com.happyandjust.nameless.features.impl.settings.FeatureHypixelAPIKey
import com.happyandjust.nameless.utils.APIUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting

object ViewStatCommand : ClientCommandBase("viewstat") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.size != 1) {
            sendUsage("[Name]")
            return
        }
        val name = args[0]

        GlobalScope.launch {
            val uuid = runCatching { APIUtils.getUUIDFromUsername(name) }.getOrElse {
                sendPrefixMessage("§cFailed to get uuid of $name")
                return@launch
            }

            val identifiers =
                FeatureInGameStatViewer.getParameterValue<List<FeatureInGameStatViewer.InGameStatIdentifier>>("order")
                    .filter { it.supportGame.shouldDisplay() }

            runCatching {
                val s = Request.get("https://api.hypixel.net/player?key=${FeatureHypixelAPIKey.apiKey}&uuid=$uuid")
                val json = JSONHandler(s).read(JsonObject())["player"].asJsonObject

                sendClientMessage("§bStats of ${getPlayerName(json)}")

                sendClientMessage(identifiers.joinToString("\n") {
                    it.informationType.run { getFormatText(getStatValue(json)) }
                })
            }.exceptionOrNull()?.notifyException()
        }
    }

    private fun getPlayerName(jsonObject: JsonObject): String {
        val displayName = jsonObject["displayname"].asString

        return "${processRank(jsonObject)}$displayName"
    }

    private fun processRank(jsonObject: JsonObject): String {
        if (jsonObject.has("prefix")) { // WTF
            return jsonObject["prefix"].asString
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

    private fun FeatureInGameStatViewer.InformationType.getFormatText(
        value: String
    ): String {
        val format = FeatureInGameStatViewer.getParameter<String>("texts").getParameterValue<String>(name.lowercase())

        return format.replace("{value}", value).replace("&", "§")
    }
}