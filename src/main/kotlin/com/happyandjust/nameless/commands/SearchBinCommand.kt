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

import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.hypixel.skyblock.BinProcessor
import gg.essential.api.commands.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SearchBinCommand : Command("searchbin") {

    @DefaultHandler
    fun handle(@DisplayName("Item Name") @Greedy name: String) {
        sendPrefixMessage("§aSearching $name...")

        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                BinProcessor(name)
                    .searchBinAuctions()
                    .notifyInChat()
                    .storeGuiCallBack()
            }.getOrElse {
                sendPrefixMessage("§c${it.message}")
                return@launch
            }
        }
    }

    @SubCommand("opengui")
    fun openGui(key: Long) = BinProcessor.openIfExists(key)
}