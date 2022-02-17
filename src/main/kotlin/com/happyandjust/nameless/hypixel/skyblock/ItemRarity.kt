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

import java.awt.Color

enum class ItemRarity(val color: Int, val colorCode: String) {

    COMMON(Color(0xFFFFFF).rgb, "§f"),
    UNCOMMON(Color(0x55FF55).rgb, "§a"),
    RARE(Color(0x5555FF).rgb, "§9"),
    EPIC(Color(0xAA00AA).rgb, "§5"),
    LEGENDARY(Color(0xFFAA00).rgb, "§6"),
    MYTHIC(Color(0xFF55FF).rgb, "§d"),
    SUPREME(Color(0xAA0000).rgb, "§4"),
    SPECIAL(Color(0xFF5555).rgb, "§c"),
    VERY_SPECIAL(Color(0xFF5555).rgb, "§c");

    val loreName = name.replace("_", " ")
}