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
import kotlinx.coroutines.DelicateCoroutinesApi
import java.util.*

object NameHistoryCommand : Command("name") {

    private fun Int.convert() = String.format("%02d", this)

    private val transformNameHistoryToString: (Name) -> String = {
        val isOriginal = it.changedToAt == 0L

        val time = it.changedToAt?.takeUnless { time -> time == 0L }?.let {
            val calendar = Calendar.getInstance().apply { time = Date(it) }

            val year = calendar[Calendar.YEAR]
            val month = (calendar[Calendar.MONTH] + 1).convert()
            val day = calendar[Calendar.DATE].convert()

            val hour = calendar[Calendar.HOUR_OF_DAY].convert()
            val minute = calendar[Calendar.MINUTE].convert()
            val second = calendar[Calendar.SECOND].convert()

            "$year-$month-$day @ $hour:$minute:$second"

        } ?: "Original Name"

        "§6${if (isOriginal) "§l" else ""}${it.name} - $time"
    }

    @OptIn(DelicateCoroutinesApi::class)
    @DefaultHandler
    fun handle(@DisplayName("Player Name") name: String) {
        Multithreading.runAsync {
            val uuid = getUUID(name) ?: run {
                sendPrefixMessage("§cFailed to get $name's uuid")
                return@runAsync
            }

            val nameHistories = getNameHistory(uuid) ?: run {
                sendPrefixMessage("§cFailed to get $name's Name History")
                return@runAsync
            }

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

    private fun getDashAsLongestString(longestWidth: Float) = "-".repeat((longestWidth / "-".width()).toInt())
}