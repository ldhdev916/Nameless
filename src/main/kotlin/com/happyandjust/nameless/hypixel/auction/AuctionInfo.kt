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

    @SerializedName("item_lore")
    var lore = ""
}