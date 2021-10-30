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

package com.happyandjust.nameless.hypixel.auction

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity

class AuctionInfo {

    @SerializedName("uuid")
    var auctionId = ""

    @SerializedName("item_name")
    var item_name = ""

    @SerializedName("starting_bid")
    var price = 0

    @SerializedName("item_bytes")
    var item_bytes = ""

    @SerializedName("bin")
    var bin = false

    @SerializedName("bids")
    var bids = JsonArray()

    @SerializedName("tier")
    var tier_string = ""

    var rarity = ItemRarity.COMMON

    var skyBlockId = ""

    @SerializedName("item_lore")
    var lore = ""

    @SerializedName("claimed")
    var claimed = false


    fun isBuyableBinAuction() = bin && bids.size() == 0 && !claimed

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuctionInfo

        if (auctionId != other.auctionId) return false
        if (item_name != other.item_name) return false
        if (price != other.price) return false
        if (item_bytes != other.item_bytes) return false
        if (bin != other.bin) return false
        if (tier_string != other.tier_string) return false
        if (rarity != other.rarity) return false
        if (skyBlockId != other.skyBlockId) return false
        if (lore != other.lore) return false
        if (claimed != other.claimed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = auctionId.hashCode()
        result = 31 * result + item_name.hashCode()
        result = 31 * result + price
        result = 31 * result + item_bytes.hashCode()
        result = 31 * result + bin.hashCode()
        result = 31 * result + tier_string.hashCode()
        result = 31 * result + rarity.hashCode()
        result = 31 * result + skyBlockId.hashCode()
        result = 31 * result + lore.hashCode()
        result = 31 * result + claimed.hashCode()
        return result
    }


}