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
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.hypixel.auction.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.network.Request
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CItemRarity
import com.happyandjust.nameless.serialization.converters.CString
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import java.util.*
import kotlin.math.abs

object FeatureTrackAuction : SimpleFeature(
    Category.SKYBLOCK,
    "trackauction",
    "Track Auction",
    "Mod will keep tracking all auctions\n§lThis may make your internet speed low and inconsistent"
), ClientTickListener {

    @Volatile
    private var scanningAuction = false

    @Volatile
    private var startTime = 0L
    private val notifyInfos = hashSetOf<NotifyInfo>()
    private var prevTotalAuctions = 0

    private val filteredSkyBlockItems = hashSetOf<String>()

    init {
        parameters["margin"] = FeatureParameter(
            0,
            "trackauction",
            "margin",
            "Min Margin",
            "Min price between 1st lowest bin of an item and 2nd lowest bin of an item",
            "3000000",
            CString
        ).also {
            it.maxStringWidth = Int.MAX_VALUE.toString().length
            it.validator = { s -> s.matches("\\d*".toRegex()) }
        }
        parameters["current"] = FeatureParameter(
            1,
            "trackauction",
            "currentmoney",
            "Current Money",
            "If this is set, mod won't show the item which is higher than your current money",
            "",
            CString
        ).also {
            it.maxStringWidth = Int.MAX_VALUE.toString().length
            it.validator = { s -> s.matches("\\d*".toRegex()) }
        }
        parameters["rarity"] = FeatureParameter(
            2,
            "trackauction",
            "minrarity",
            "Minimum Rarity",
            "",
            ItemRarity.COMMON,
            CItemRarity
        ).also {
            it.allEnumList = ItemRarity.values().toList()
        }
        parameters["filtername"] = FeatureParameter(
            3,
            "trackauction",
            "filtername",
            "Filter Item Name",
            "Prevent certain item from being searched if its name contains value you set\nSplit with comma(,)",
            "",
            CString
        )
    }

    fun updateItemData() {
        parameters["filter"] = FeatureParameter(
            3,
            "trackauction",
            "filter",
            "Filter Certain SkyBlock Item",
            "Prevent certain item from being searched",
            false,
            CBoolean
        ).also {
            for (skyBlockItem in SkyblockUtils.allItems.values) {
                val id = skyBlockItem.id.lowercase()

                it.parameters[id] = FeatureParameter(
                    0,
                    "trackauctionfilter",
                    id,
                    skyBlockItem.name,
                    "SkyBlock ID: ${skyBlockItem.id}",
                    false,
                    CBoolean
                ).also { featureParameter ->
                    featureParameter.onToggleClick = { b ->
                        if (b) {
                            filteredSkyBlockItems.add(skyBlockItem.id)
                        } else {
                            filteredSkyBlockItems.remove(skyBlockItem.id)
                        }
                    }

                    if (skyBlockItem.id.contains("THE_FISH")) featureParameter.value = true

                    if (featureParameter.value) filteredSkyBlockItems.add(skyBlockItem.id)
                }
            }
        }
    }

    private fun getTotalAuction() =
        JSONHandler(Request.get("https://api.hypixel.net/skyblock/auctions")).read(JsonObject())["totalAuctions"].asInt

    override fun tick() {
        if (!enabled) return
        if (scanningAuction) return
        scanningAuction = true

        threadPool.execute {

            val totalAuctions = getTotalAuction()

            if (prevTotalAuctions == totalAuctions) {
                scanningAuction = false
                return@execute
            }
            prevTotalAuctions = totalAuctions

            startTime = System.currentTimeMillis()
            scanAuction(::doTask)
        }
    }

    private fun doTask(allAuctions: List<AuctionInfo>) {

        val sec = ((System.currentTimeMillis() - startTime) / 1000.0).transformToPrecision(3).formatDouble()

        sendClientMessage("§aDone Scanning Auctions Time Since Start: $sec")

        try {
            val margin = getParameterValue<String>("margin").toIntOrNull() ?: return
            val currentMoney = getParameterValue<String>("current").toIntOrNull() ?: Int.MAX_VALUE
            val minRarity = getParameterValue<ItemRarity>("rarity")

            val shouldFilter = getParameterValue<Boolean>("filter")
            val shouldFilterNames = getParameterValue<String>("filtername").split(",")

            val containsOneOf: (String) -> Boolean = f@{
                for (filterNames in shouldFilterNames) {
                    if (it.contains(filterNames, true)) return@f true
                }
                false
            }


            val binAuctionInfos = hashMapOf<String, PriorityQueue<AuctionInfo>>()

            val compare = compareBy<AuctionInfo> { it.price }

            for (auctionInfo in allAuctions) {
                if (auctionInfo.skyBlockId.isEmpty()) continue
                if (!auctionInfo.isBuyableBinAuction()) continue
                if (shouldFilter && filteredSkyBlockItems.contains(auctionInfo.skyBlockId)) continue
                if (containsOneOf(auctionInfo.item_name)) continue

                val existing = binAuctionInfos[auctionInfo.skyBlockId] ?: PriorityQueue(compare)

                existing.add(auctionInfo)

                binAuctionInfos[auctionInfo.skyBlockId] = existing
            }

            for ((_, auctionInfos) in binAuctionInfos) {
                if (auctionInfos.size < 2) continue

                val first = auctionInfos.poll()

                if (first.price > currentMoney) continue
                if (first.rarity < minRarity) continue

                val second = auctionInfos.poll()

                if (second.price - first.price >= margin) {

                    val isRarityDiff = first.rarity != second.rarity

                    val chat = getChatTextForAuctionInfo(first).appendText(
                        " §6${first.price.insertCommaEvery3Character()} -> ${second.price.insertCommaEvery3Character()}"
                    )
                    if (isRarityDiff) {
                        chat.appendText(
                            "\n§4Rarity is different ${first.rarity.colorCode}${first.rarity} §4-> ${second.rarity.colorCode}${second.rarity}"
                        )
                    }

                    if (notifyInfos.add(NotifyInfo(first, System.currentTimeMillis()))) {
                        sendClientMessage(chat.appendText("\n"))
                    }
                }
            }
        } catch (e: Exception) {
            e.notifyException()
        }

        scanningAuction = false
    }

    fun getChatTextForAuctionInfo(auctionInfo: AuctionInfo): IChatComponent {
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

    class NotifyInfo(val auctionInfo: AuctionInfo, val time: Long) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as NotifyInfo

            if (auctionInfo != other.auctionInfo) return false

            if (abs(time - other.time) > 10 * 60 * 1000L) return false

            return true
        }

        override fun hashCode(): Int {
            return auctionInfo.hashCode()
        }
    }
}