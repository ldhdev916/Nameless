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

package com.happyandjust.nameless.features.impl.skyblock

import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.hypixel.auction.AuctionInfo
import com.happyandjust.nameless.network.Request
import com.happyandjust.nameless.serialization.TypeRegistry
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import net.minecraftforge.common.util.Constants
import java.io.ByteArrayInputStream
import java.util.*

class FeatureTrackAuction : SimpleFeature(
    Category.SKYBLOCK,
    "trackauction",
    "Track Auction",
    "Set items and prices via commands, mod will keep tracking all auctions\nAnd if there's a one whose price(bin) is lower than the one you set before, mods will notify you"
), ClientTickListener {

    private val tracks = hashMapOf<String, Int>()
    private var prevTotalAuctions = -1
    private var lastScanAuction = -1L

    init {
        val cBoolean = TypeRegistry.getConverterByClass(Boolean::class)
        val cString = TypeRegistry.getConverterByClass(String::class)

        for (skyBlockItem in SkyblockUtils.allItems.values) {
            val id = skyBlockItem.id.lowercase()

            parameters[id] = FeatureParameter(
                0,
                "trackauction",
                id,
                skyBlockItem.name,
                "",
                false,
                cBoolean
            ).also {
                it.parameters["price"] = FeatureParameter(
                    0,
                    "trackauction",
                    "${id}_price",
                    "Lowest Price of ${skyBlockItem.name}",
                    "",
                    "1",
                    cString
                ).also { parameter ->
                    parameter.maxStringWidth = 10
                    parameter.validator = { text -> text.matches("\\d*".toRegex()) }
                }
            }
        }
    }

    override fun tick() {
        if (!enabled) return

        threadPool.execute {

            if (lastScanAuction + 60000 > System.currentTimeMillis()) return@execute

            tracks.clear()

            for (enabledItem in parameters.values.filter { it.value as Boolean }) {
                tracks[enabledItem.title] = enabledItem.getParameterValue<String>("price").toInt()
            }

            if (tracks.isEmpty()) return@execute // do we rly need to get all auction data even if you don't need it smh

            lastScanAuction = System.currentTimeMillis()

            repeat(getMaxAuctionPage()) {
                for (auctionInfo in SkyblockUtils.getAuctionDataInPage(it)
                    .filter { data -> data.bin && data.bids.size() == 0 }) {

                    if (!isMatchingPair(auctionInfo)) continue

                    sendClientMessage(getChatTextForAuctionInfo(auctionInfo))
                }
            }
        }
    }

    private fun getMaxAuctionPage(): Int {
        val s = Request.get("https://api.hypixel.net/skyblock/auctions")

        val json = JSONHandler(s).read(JsonObject())

        if (!json["success"].asBoolean) return 0

        val totalAuctions = json["totalAuctions"].asInt

        if (totalAuctions == prevTotalAuctions) return 0
        prevTotalAuctions = totalAuctions

        return json["totalPages"].asInt
    }

    private fun getChatTextForAuctionInfo(auctionInfo: AuctionInfo): IChatComponent {
        try {
            val textComponent =
                ChatComponentText("§aFound §6${auctionInfo.item_name} §awith Price §6${auctionInfo.price}\n")

            val openAuction = ChatComponentText("§a[VIEW AUCTION] ").also {
                it.chatStyle = ChatStyle().setChatClickEvent(
                    ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/viewauction ${auctionInfo.auctionId}"
                    )
                )
            }

            val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(auctionInfo.item_bytes)).buffered()

            val nbt = CompressedStreamTools.readCompressed(inputStream)

            val itemStackCompound = nbt.getTagList("i", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0)

            val viewItem = ChatComponentText("§e[VIEW ITEM]").also {
                it.chatStyle = ChatStyle().setChatHoverEvent(
                    HoverEvent(
                        HoverEvent.Action.SHOW_ITEM,
                        ChatComponentText(itemStackCompound.toString())
                    )
                )
            }

            textComponent.appendSibling(openAuction).appendSibling(viewItem)

            return textComponent
        } catch (e: Exception) {
            return ChatComponentText("§cERROR ${e.javaClass.name} ${e.message}")
        }
    }

    private fun isMatchingPair(auctionInfo: AuctionInfo): Boolean {

        val name = auctionInfo.item_name

        for (track in tracks) {
            if (name.contains(track.key, true) && auctionInfo.price <= track.value) {
                return true
            }
        }

        return false
    }
}