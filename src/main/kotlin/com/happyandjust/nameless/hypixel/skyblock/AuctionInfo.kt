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
data class AuctionInfo(
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
}