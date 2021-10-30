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

package com.happyandjust.nameless.hypixel

enum class GameType(val displayName: String, vararg val modeReqs: String) {

    SKYBLOCK("SKYBLOCK"),
    MURDER_MYSTERY("MURDER_MYSTERY"),
    BEDWARS("BEDWARS"),
    GUESS_THE_BUILD("BUILD_BATTLE", "BUILD_BATTLE_GUESS_THE_BUILD"),
    PARTY_GAMES("ARCADE", "PARTY"),
    SKYWARS("SKYWARS"),
    PIXEL_PARTY("PROTOTYPE", "PIXEL_PARTY")

}
