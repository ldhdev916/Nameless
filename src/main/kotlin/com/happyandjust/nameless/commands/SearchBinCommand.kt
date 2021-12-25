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

import com.happyandjust.nameless.dsl.scanAuction
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.gui.auction.AuctionGui
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import gg.essential.api.commands.*
import gg.essential.api.utils.GuiUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle

object SearchBinCommand : Command("searchbin") {

    private var openGui: (() -> AuctionGui)? = null

    @OptIn(DelicateCoroutinesApi::class)
    @DefaultHandler
    fun handle(@DisplayName("Item Name") @Greedy name: String) {
        sendPrefixMessage("§aSearching $name...")

        GlobalScope.launch {
            val auctionInfos = arrayListOf<AuctionInfo>()

            scanAuction { list ->
                auctionInfos.addAll(
                    list.filter { it.isBuyableBinAuction() && it.item_name.contains(name, true) }
                )

                if (auctionInfos.isEmpty()) {
                    sendPrefixMessage("§cNo bin auction found for item $name")
                    return@scanAuction
                }

                if (auctionInfos.size >= 700) {
                    sendPrefixMessage("§cSo many items! (${auctionInfos.size}) try pass item name more specific")
                    return@scanAuction
                }

                val click = ChatComponentText(" §a§l[CLICK]").apply {

                    val hoverText = ChatComponentText("§aClick Here to View Items!")

                    chatStyle = ChatStyle()
                        .setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/searchbin opengui"))
                        .setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
                }

                openGui = { AuctionGui(auctionInfos) }
                sendPrefixMessage(ChatComponentText("§aFound total ${auctionInfos.size} items!").appendSibling(click))
            }
        }
    }

    @SubCommand("opengui")
    fun openGui() {
        openGui?.let {
            GuiUtil.open(it())
            return
        }
    }

}