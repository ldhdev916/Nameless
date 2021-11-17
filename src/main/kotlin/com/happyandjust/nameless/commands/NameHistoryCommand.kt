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
import net.minecraft.command.ICommandSender
import java.util.*
import kotlin.concurrent.thread

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
        thread {
            if (args.size != 1) {
                sendPrefixMessage("§cUsage: /name [Player Name]")
                return@thread
            }

            val name = args[0]

            val uuid = try {
                APIUtils.getUUIDFromUsername(name)
            } catch (e: RuntimeException) {
                sendPrefixMessage("§cFailed to get $name's uuid")
                return@thread
            }

            val nameHistoryList = try {
                APIUtils.getNameHistoryFromUUID(uuid)
            } catch (e: RuntimeException) {
                sendPrefixMessage("§cFailed to get $name's Name History")
                return@thread
            }

            val fontRenderer = mc.fontRendererObj

            val nameHistoryTexts = nameHistoryList.map(transformNameHistoryToString)

            if (nameHistoryTexts.isEmpty()) {
                sendPrefixMessage("§cSomething went wrong! Try again")
                return@thread
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