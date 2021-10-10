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
import com.happyandjust.nameless.devqol.convertToStringList
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.hypixel.auction.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import java.util.*
import kotlin.concurrent.thread

class SearchBinCommand : ClientCommandBase("searchbin") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.size < 3) {
            sendClientMessage("§cUsage: /searchbin [lowest/all] [Rarity] [Item Name]")
            return
        }

        val method = args[0].lowercase()

        val rarity = try {
            ItemRarity.fromString(args[1].uppercase())
        } catch (e: IllegalArgumentException) {
            sendClientMessage("§c${e.message}")
            return
        }

        val name = args.toList().subList(2, args.size).joinToString(" ")

        sendClientMessage("§aSearching $name...")
        sendClientMessage("§aThis may take up like 1 minute")

        val feature = FeatureRegistry.TRACK_AUCTION

        thread {

            val priorityQueue = PriorityQueue<AuctionInfo>(compareBy { auctionInfo -> auctionInfo.price })

            repeat(feature.getMaxAuctionPage()) {
                for (auctionInfo in SkyblockUtils.getAuctionDataInPage(it)
                    .filter { data -> data.bin && data.bids.size() == 0 }) {
                    if (!auctionInfo.item_name.contains(name, true)) continue
                    if (auctionInfo.rarity != rarity) continue

                    priorityQueue.add(auctionInfo)
                }
            }

            if (priorityQueue.isEmpty()) {
                sendClientMessage("§cNo Bin Found for Item $name")
                return@thread
            }

            when (method) {
                "all" -> {
                    while (priorityQueue.isNotEmpty()) {
                        sendClientMessage(feature.getChatTextForAuctionInfo(priorityQueue.poll()))
                    }
                }
                "lowest" -> {
                    sendClientMessage(feature.getChatTextForAuctionInfo(priorityQueue.peek()))
                }
                else -> sendClientMessage("No Such Method $method")
            }
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos
    ): MutableList<String> {

        return when (args.size) {
            1 -> listOf("all", "lowest").filter { it.startsWith(args[0], true) }.toMutableList()
            2 -> {
                ItemRarity.values().filter { it.webName.startsWith(args[1], true) }
                    .convertToStringList { it.webName }.toMutableList()
            }
            else -> mutableListOf()
        }
    }
}