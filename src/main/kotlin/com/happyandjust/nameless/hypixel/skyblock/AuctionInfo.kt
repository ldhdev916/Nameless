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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class AuctionInfo(
    @SerialName("uuid") val auctionId: String,
    val item_name: String,
    @SerialName("starting_bid") val price: Int,
    val item_bytes: String,
    val bin: Boolean,
    val bids: List<JsonObject>,
    @SerialName("tier") val rarity: ItemRarity,
    @SerialName("item_lore") val lore: String,
    val claimed: Boolean
) {
    var skyBlockId = ""

    fun isBuyableBinAuction() = bin && bids.isEmpty() && !claimed

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuctionInfo

        if (auctionId != other.auctionId) return false
        if (item_name != other.item_name) return false
        if (price != other.price) return false
        if (item_bytes != other.item_bytes) return false
        if (bin != other.bin) return false
        if (bids != other.bids) return false
        if (rarity != other.rarity) return false
        if (lore != other.lore) return false
        if (claimed != other.claimed) return false
        if (skyBlockId != other.skyBlockId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = auctionId.hashCode()
        result = 31 * result + item_name.hashCode()
        result = 31 * result + price
        result = 31 * result + item_bytes.hashCode()
        result = 31 * result + bin.hashCode()
        result = 31 * result + bids.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + lore.hashCode()
        result = 31 * result + claimed.hashCode()
        result = 31 * result + skyBlockId.hashCode()
        return result
    }


}