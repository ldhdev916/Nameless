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

import java.awt.Color

enum class ItemRarity(val webName: String, val color: Int, val loreName: String, val colorCode: String) {


    COMMON("COMMON", Color(0xFFFFFF).rgb, "COMMON", "§f"),
    UNCOMMON("UNCOMMON", Color(0x55FF55).rgb, "UNCOMMON", "§a"),
    RARE("RARE", Color(0x5555FF).rgb, "RARE", "§9"),
    EPIC("EPIC", Color(0xAA00AA).rgb, "EPIC", "§5"),
    LEGENDARY("LEGENDARY", Color(0xFFAA00).rgb, "LEGENDARY", "§6"),
    MYTHIC("MYTHIC", Color(0xFF55FF).rgb, "MYTHIC", "§d"),
    SUPREME("SUPREME", Color(0xAA0000).rgb, "SUPREME", "§4"),
    SPECIAL("SPECIAL", Color(0xFF5555).rgb, "SPECIAL", "§c"),
    VERY_SPECIAL("VERY_SPECIAL", Color(0xFF5555).rgb, "VERY SPECIAL", "§c");

    companion object {

        private val values = values()

        fun fromString(name: String): ItemRarity {
            for (rarity in values) {
                if (rarity.webName == name) return rarity
            }

            throw IllegalArgumentException("No Such ItemRarity: $name")
        }
    }
}