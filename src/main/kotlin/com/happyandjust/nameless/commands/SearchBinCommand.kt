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
import com.happyandjust.nameless.devqol.insertCommaEvery3Character
import com.happyandjust.nameless.devqol.scanAuction
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.hypixel.auction.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import java.util.*
import kotlin.concurrent.thread

object SearchBinCommand : ClientCommandBase("searchbin") {

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

        thread {
            val priorityQueue = PriorityQueue<AuctionInfo>(compareBy { auctionInfo -> auctionInfo.price })

            scanAuction {
                for (auctionInfo in it.filter { auctionInfo -> auctionInfo.isBuyableBinAuction() }) {
                    if (!auctionInfo.item_name.contains(name, true)) continue
                    if (auctionInfo.rarity != rarity) continue

                    priorityQueue.add(auctionInfo)
                }

                if (priorityQueue.isEmpty()) {
                    sendClientMessage("§cNo Bin Found for Item $name")
                    return@scanAuction
                }

                when (method) {
                    "all" -> {
                        while (priorityQueue.isNotEmpty()) {
                            sendClientMessage(getChatTextForAuctionInfo(priorityQueue.poll()))
                        }
                    }
                    "lowest" -> {
                        sendClientMessage(getChatTextForAuctionInfo(priorityQueue.peek()))
                    }
                    else -> sendClientMessage("No Such Method $method")
                }
            }
        }
    }

    private fun getChatTextForAuctionInfo(auctionInfo: AuctionInfo): IChatComponent {
        return try {
            val textComponent =
                ChatComponentText("§aFound ${auctionInfo.rarity.colorCode}${auctionInfo.item_name} §awith Price §6${auctionInfo.price.insertCommaEvery3Character()}\n")

            val openAuction = ChatComponentText("§a[VIEW AUCTION] ").also {
                it.chatStyle = ChatStyle().setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/viewauction ${auctionInfo.auctionId}"
                    )
                )
            }

            val itemStackCompound = SkyblockUtils.readNBTFromItemBytes(auctionInfo.item_bytes)

            val viewItem = ChatComponentText("§e[VIEW ITEM]").also {
                it.chatStyle = ChatStyle().setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        ChatComponentText(itemStackCompound.toString())
                    )
                )
            }

            textComponent.appendSibling(openAuction).appendSibling(viewItem)

            textComponent
        } catch (e: Exception) {
            ChatComponentText("§cERROR ${e.javaClass.name} ${e.message}")
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
                    .map { it.webName }.toMutableList()
            }
            else -> mutableListOf()
        }
    }
}