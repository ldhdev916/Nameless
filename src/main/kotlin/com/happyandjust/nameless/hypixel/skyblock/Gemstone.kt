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

enum class Gemstone(val color: Int, val metadata: Int, val readableName: String) {

    RUBY(Color(255, 0, 0, 40).rgb, 14, "Ruby"),
    AMETHYST(Color(128, 0, 128, 40).rgb, 10, "Amethyst"),
    JADE(Color(0, 255, 0, 40).rgb, 5, "Jade"),
    SAPPHIRE(Color(0, 255, 255, 40).rgb, 3, "Sapphire"),
    AMBER(Color(255, 128, 0, 40).rgb, 1, "Amber"),
    TOPAZ(Color(255, 255, 0, 40).rgb, 4, "Topaz"),
    JASPER(Color(255, 0, 255, 40).rgb, 2, "Jasper")
}