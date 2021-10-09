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

enum class DungeonFloor(val scoreboardName: String, val floorInt: Int) {

    ENTRANCE("E", 0),
    FLOOR_1("F1", 1), FLOOR_2("F2", 2), FLOOR_3("F3", 3), FLOOR_4("F4", 4),
    FLOOR_5("F5", 5), FLOOR_6("F6", 6), FLOOR_7("F7", 7),
    MASTER_1("M1", 1), MASTER_2("M2", 2), MASTER_3("M3", 3),
    MASTER_4("M4", 4), MASTER_5("M5", 5), MASTER_6("M6", 6);

    companion object {

        private val values = values()

        fun getByScoreboardName(s: String): DungeonFloor? {
            for (value in values) {
                if (s == value.scoreboardName) return value
            }

            return null
        }

    }
}