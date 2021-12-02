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

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.gui.shortcmd.ShortCommandGui
import com.happyandjust.nameless.serialization.Converter
import gg.essential.api.EssentialAPI
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object ShortCommand : Command("shortcommand") {

    private val gson = Gson()
    var shortCommandInfos by ConfigValue("shortcommand", "list", emptyList(), CShortCommandInfoList)

    override val commandAliases = hashSetOf(Alias("shortcmd"))

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @DefaultHandler
    fun handle() {
        EssentialAPI.getGuiUtil().openScreen(ShortCommandGui())
    }

    @SubscribeEvent
    fun onPacketSending(e: PacketEvent.Sending) {
        val msg = e.packet

        if (msg is C01PacketChatMessage) {
            val (shortCommandInfo, matcher) = shortCommandInfos.map { it to it.pair.first.matcher(msg.message) }
                .find { it.second.matches() } ?: return

            val groups = (0 until shortCommandInfo.pair.second).map { matcher.group("g$it") }
            var newString = shortCommandInfo.origin

            for (group in groups) {
                newString = newString.replaceFirst("{}", group)
            }

            e.packet = C01PacketChatMessage(newString)

        }
    }

    data class ShortCommandInfo(
        @SerializedName("short") var short: String,
        @SerializedName("origin") var origin: String
    ) {
        val pair: Pair<Pattern, Int>
            get() {
                var index = 0
                var newString = short

                while ("{}" in newString) {
                    newString = newString.replaceFirst("{}", "(?<g${index++}>\\w+)")
                }

                return newString.toPattern() to index
            }
    }

    object CShortCommandInfoList : Converter<List<ShortCommandInfo>> {

        override fun serialize(t: List<ShortCommandInfo>): JsonElement {
            val jsonArray = JsonArray()

            for (shortCommandInfo in t) {
                jsonArray.add(gson.fromJson(gson.toJson(shortCommandInfo), JsonObject::class.java))
            }

            return jsonArray
        }

        override fun deserialize(jsonElement: JsonElement): List<ShortCommandInfo> {
            val list = arrayListOf<ShortCommandInfo>()

            for (jsonObject in jsonElement.asJsonArray) {
                list.add(gson.fromJson(jsonObject, ShortCommandInfo::class.java))
            }

            return list
        }


    }

}