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

import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.core.NameHistory
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.sendClientMessage
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.utils.APIUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.command.ICommandSender
import java.util.*

object NameHistoryCommand : ClientCommandBase("name") {

    private fun Int.convert() = String.format("%02d", this)

    private val transformNameHistoryToString: (NameHistory) -> String = {
        val isOriginal = it.timestamp == null

        val time = it.timestamp?.let {
            val calendar = Calendar.getInstance().apply { time = Date(it) }

            val year = calendar[Calendar.YEAR]
            val month = (calendar[Calendar.MONTH] + 1).convert()
            val day = calendar[Calendar.DATE].convert()

            val hour = calendar[Calendar.HOUR_OF_DAY].convert()
            val minute = calendar[Calendar.MINUTE].convert()
            val second = calendar[Calendar.SECOND].convert()

            "$year-$month-$day @ $hour:$minute:$second"

        } ?: "Original Name"

        "§6${if (isOriginal) "§l" else ""}${it.username} - $time"
    }


    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.size != 1) {
            sendUsage("[Player Name]")
            return
        }
        val name = args[0]
        GlobalScope.launch {
            val uuid = runCatching { APIUtils.getUUIDFromUsername(name) }.getOrElse {
                sendPrefixMessage("§cFailed to get $name's uuid")
                return@launch
            }

            val nameHistories = runCatching { APIUtils.getNameHistoryFromUUID(uuid) }.getOrElse {
                sendPrefixMessage("§cFailed to get $name's Name History")
                return@launch
            }

            val fontRenderer = mc.fontRendererObj

            val nameHistoryTexts = nameHistories.map(transformNameHistoryToString)

            if (nameHistoryTexts.isEmpty()) {
                sendPrefixMessage("§cSomething went wrong! Try again")
                return@launch
            }

            val dash = "§6${getDashAsLongestString(nameHistoryTexts.maxOf { fontRenderer.getStringWidth(it) })}"

            sendClientMessage(dash)
            sendClientMessage(nameHistoryTexts.joinToString("\n"))
            sendClientMessage(dash)

        }
    }

    private fun getDashAsLongestString(longestWidth: Int): String {
        val builder = StringBuilder()

        val fontRenderer = mc.fontRendererObj

        while (fontRenderer.getStringWidth(builder.toString()) <= longestWidth) {
            builder.append("-")
        }

        return builder.toString()
    }
}