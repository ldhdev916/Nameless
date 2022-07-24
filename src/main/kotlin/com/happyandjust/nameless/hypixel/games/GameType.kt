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

package com.happyandjust.nameless.hypixel.games

import com.happyandjust.nameless.hypixel.LocrawInfo
import com.happyandjust.nameless.hypixel.partygames.PartyMiniGamesFactoryImpl

interface GameType {

    fun handleProperty(locrawInfo: LocrawInfo)

    fun printProperties()

    fun onDisposed() {}
}

fun interface GameTypeFactory {
    fun createGameType(locrawInfo: LocrawInfo): GameType?
}

object GameTypeFactoryImpl : GameTypeFactory {
    override fun createGameType(locrawInfo: LocrawInfo) = with(locrawInfo) {
        when {
            gameType == "BEDWARS" -> BedWars()
            gameType == "ARCADE" && mode == "GRINCH_SIMULATOR_V2" -> GrinchSimulator()
            gameType == "BUILD_BATTLE" && mode == "BUILD_BATTLE_GUESS_THE_BUILD" -> GuessTheBuild()
            mode == "lobby" -> Lobby()
            gameType == "MURDER_MYSTERY" -> MurderMystery()
            gameType == "ARCADE" && mode == "PARTY" -> PartyGames(PartyMiniGamesFactoryImpl)
            gameType == "PROTOTYPE" && mode == "PIXEL_PARTY" -> PixelParty()
            gameType == "SKYBLOCK" -> SkyBlock()
            gameType == "SKYWARS" -> SkyWars()
            else -> null
        }
    }
}