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

import com.happyandjust.nameless.dsl.getNameHistory
import com.happyandjust.nameless.dsl.getUUID
import com.happyandjust.nameless.dsl.sendClientMessage
import com.happyandjust.nameless.dsl.sendPrefixMessage
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.DisplayName
import gg.essential.api.utils.Multithreading
import gg.essential.api.utils.mojang.Name
import gg.essential.elementa.dsl.width
import java.util.*

object NameHistoryCommand : Command("name") {

    private val transformNameHistoryToString: (Name) -> String = {
        val isOriginal = it.changedToAt == 0L

        val time = it.changedToAt?.takeUnless { time -> time == 0L }?.let {
            val calendar = Calendar.getInstance().apply { time = Date(it) }

            val year = calendar[Calendar.YEAR]
            val month = "%02d".format(calendar[Calendar.MONTH] + 1)
            val day = "%02d".format(calendar[Calendar.DATE])

            val hour = "%02d".format(calendar[Calendar.HOUR_OF_DAY])
            val minute = "%02d".format(calendar[Calendar.MINUTE])
            val second = "%02d".format(calendar[Calendar.SECOND])

            "$year-$month-$day @ $hour:$minute:$second"

        } ?: "Original Name"

        buildString {
            append("§6")
            if (isOriginal) {
                append("§l")
            }
            append("${it.name} - $time")
        }
    }

    @DefaultHandler
    fun handle(@DisplayName("Player Name") name: String) {
        Multithreading.runAsync {
            val nameHistories = getNameHistories(name) ?: return@runAsync

            val nameHistoryTexts = nameHistories.map(transformNameHistoryToString).ifEmpty {
                sendPrefixMessage("§cSomething went wrong! Try again")
                return@runAsync
            }

            val dash = "§6${getDashAsLongestString(nameHistoryTexts.maxOf(String::width))}"

            sendClientMessage(dash)
            sendClientMessage(nameHistoryTexts.joinToString("\n"))
            sendClientMessage(dash)

        }
    }

    private fun getNameHistories(name: String): List<Name>? {
        val uuid = getUUID(name) ?: run {
            sendPrefixMessage("§cFailed to get $name's uuid")
            return null
        }

        val nameHistories = getNameHistory(uuid) ?: run {
            sendPrefixMessage("§cFailed to get $name's Name History")
            return null
        }
        return nameHistories
    }

    private fun getDashAsLongestString(longestWidth: Float) = "-".repeat((longestWidth / "-".width()).toInt())
}