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
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.gui.shortcmd.ShortCommandGui
import com.happyandjust.nameless.serialization.converters.CList
import com.happyandjust.nameless.serialization.toConverter
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.utils.GuiUtil
import net.minecraft.network.play.client.C01PacketChatMessage
import java.util.regex.Pattern

object ShortCommand : Command("shortcommand") {

    private val gson = Gson()
    var shortCommandInfos by ConfigValue(
        "shortcommand",
        "list",
        emptyList(),
        CList<ShortCommandInfo>(gson.toConverter())
    )

    override val commandAliases = hashSetOf(Alias("shortcmd"))

    init {
        on<PacketEvent.Sending>().subscribe {
            packet.withInstance<C01PacketChatMessage> {
                val (shortCommandInfo, matcher) = shortCommandInfos.map { it to it.pair.first.matcher(message) }
                    .find { it.second.matches() } ?: return@subscribe

                val groups = List(shortCommandInfo.pair.second) { matcher.group("g$it") }
                val newText = groups.fold(shortCommandInfo.origin) { acc, s -> acc.replaceFirst("{}", s) }
                packet = C01PacketChatMessage(newText)
            }
        }
    }

    @DefaultHandler
    fun handle() {
        GuiUtil.open(ShortCommandGui())
    }

    data class ShortCommandInfo(var short: String, var origin: String) {
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

}