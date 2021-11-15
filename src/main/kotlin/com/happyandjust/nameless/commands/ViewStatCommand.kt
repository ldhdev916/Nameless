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
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.devqol.sendPrefixMessage
import com.happyandjust.nameless.features.impl.qol.FeatureInGameStatViewer
import com.happyandjust.nameless.features.impl.settings.FeatureHypixelAPIKey
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.network.Request
import com.happyandjust.nameless.utils.APIUtils
import net.minecraft.command.ICommandSender
import net.minecraft.util.EnumChatFormatting
import kotlin.concurrent.thread

object ViewStatCommand : ClientCommandBase("viewstat") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.size != 1) {
            sendPrefixMessage("§cUsage: /viewstat [Name]")
            return
        }
        val name = args[0]

        thread {
            val uuid = try {
                APIUtils.getUUIDFromUsername(name)
            } catch (e: RuntimeException) {
                sendPrefixMessage("§cFailed to get uuid of $name")
                return@thread
            }

            val identifiers = FeatureInGameStatViewer.getParameterValue<List<Identifier>>("order")
                .map { it as FeatureInGameStatViewer.InGameStatIdentifier }.filter { it.supportGame.shouldDisplay() }

            try {
                val builder = StringBuilder()

                val s = Request.get("https://api.hypixel.net/player?key=${FeatureHypixelAPIKey.apiKey}&uuid=$uuid")
                val json = JSONHandler(s).read(JsonObject())["player"].asJsonObject

                builder.append("§bStats of §e${getPlayerName(json)}")

                for (identifier in identifiers) {
                    builder.append("\n")
                        .append(identifier.informationType.getFormatText(identifier.informationType.getStatValue(json)))
                }

                sendClientMessage(builder)
            } catch (e: Exception) {
                sendPrefixMessage("§cError occurred while getting player's stats Reason: ${e.javaClass.name} ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun getPlayerName(jsonObject: JsonObject): String {
        val displayName = jsonObject["displayname"].asString

        return "${processRank(jsonObject)} $displayName"
    }

    private fun processRank(jsonObject: JsonObject): String {
        if (jsonObject.has("prefix")) { // WTF
            return jsonObject["prefix"].asString
        }

        if (jsonObject.has("rank")) {
            when (jsonObject["rank"].asString) {
                "ADMIN" -> return "§c[ADMIN]"
                "MODERATOR" -> return "§2[MOD]"
                "HELPER" -> return "§9[HELPER]"
                "YOUTUBER" -> return "§c[§fYOUTUBE§c]"
            }
        }

        return when ((if (jsonObject.has("packageRank")) jsonObject["packageRank"] else jsonObject["newPackageRank"]).asString) {
            "MVP_PLUS" -> {
                val plus = if (jsonObject.has("rankPlusColor")) jsonObject["rankPlusColor"].asString else "RED"
                val color = EnumChatFormatting.valueOf(plus)

                if (jsonObject["monthlyPackageRank"].asString == "SUPERSTAR") {
                    "§6[MVP$color++§6]"
                } else {
                    "§b[MVP$color+§b]"
                }
            }
            "MVP" -> "§b[MVP]"
            "VIP_PLUS" -> "§a[VIP§6+§a]"
            "VIP" -> "§a[VIP]"
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