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

package com.happyandjust.nameless.hypixel.skyblock

import com.google.gson.annotations.SerializedName

class SkyBlockItem {

    @SerializedName("name")
    var name = ""

    @SerializedName("id")
    var id = ""

    @SerializedName("tier")
    var stringRarity = "COMMON"
        set(value) {
            field = value
            rarity = ItemRarity.fromString(value)
        }

    @SerializedName("skin")
    var skin = ""

    var rarity = ItemRarity.COMMON

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SkyBlockItem

        if (name != other.name) return false
        if (id != other.id) return false
        if (stringRarity != other.stringRarity) return false
        if (skin != other.skin) return false
        if (rarity != other.rarity) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + stringRarity.hashCode()
        result = 31 * result + skin.hashCode()
        result = 31 * result + rarity.hashCode()
        return result
    }


}