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

package com.happyandjust.nameless.hypixel.skyblock

import com.happyandjust.nameless.dsl.boolean
import com.happyandjust.nameless.dsl.fetch
import com.happyandjust.nameless.dsl.int
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.gui.auction.AuctionGui
import gg.essential.api.utils.GuiUtil
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.util.Constants
import java.util.*

class BinProcessor(private val searchName: String) {

    private val cachedBinAuctions = arrayListOf<AuctionInfo>()

    fun searchBinAuctions() = apply {
        runBlocking {
            val asyncList = List(getTotalAuctionPage()) {
                async {
                    runCatching {
                        getAuctionsAtPage(it).filter(AuctionInfo::isBuyableBinAuction)
                    }.getOrDefault(emptyList())
                }
            }

            cachedBinAuctions.addAll(asyncList.flatMap { it.await() })
        }
    }

    fun notifyInChat() = apply {
        checkCachedAuctions()
        sendPrefixMessage(getChatComponent())
    }

    fun storeGuiCallBack() {
        cachedAuctionGuiCallbacks[System.currentTimeMillis()] = { AuctionGui(cachedBinAuctions) }
    }

    private fun checkCachedAuctions() {
        val size = cachedBinAuctions.size
        when {
            size == 0 -> error("No bin auction found for item $searchName")
            size >= 700 -> error("So many items! ($size) try pass item name more specific")
        }
    }

    private fun getChatComponent(): IChatComponent? {
        val click = ChatComponentText(" §a§l[CLICK]").apply {
            val hoverText = ChatComponentText("§aClick Here to View Items!")

            chatStyle = ChatStyle()
                .setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/searchbin opengui"))
                .setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))
        }
        return ChatComponentText("§aFound total ${cachedBinAuctions.size} items!").appendSibling(click)
    }

    private fun getAuctionsAtPage(page: Int): List<AuctionInfo> {
        val jsonObject =
            Json.decodeFromString<JsonObject>("https://api.hypixel.net/skyblock/auctions?page=$page".fetch())

        if (jsonObject["success"]?.boolean != true) return emptyList()

        return Json.decodeFromJsonElement<List<AuctionInfo>>(jsonObject["auctions"]!!).onEach {
            it.skyBlockId = findSkyBlockID(it)
        }
    }

    private fun findSkyBlockID(it: AuctionInfo): String {
        val inputStream = Base64.getDecoder().decode(it.item_bytes).inputStream()

        val nbt = CompressedStreamTools.readCompressed(inputStream)
        return nbt.getTagList("i", Constants.NBT.TAG_COMPOUND)
            .getCompoundTagAt(0)
            .getCompoundTag("tag")
            .getCompoundTag("ExtraAttributes")
            .getString("id")
    }

    private fun getTotalAuctionPage(): Int {
        val json = Json.decodeFromString<JsonObject>("https://api.hypixel.net/skyblock/auctions".fetch())

        return json["totalPages"]?.int ?: 0
    }

    companion object {
        private val cachedAuctionGuiCallbacks = hashMapOf<Long, () -> AuctionGui>()

        fun openIfExists(key: Long) {
            cachedAuctionGuiCallbacks[key]?.let { GuiUtil.open(it()) }
        }
    }
}