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

import com.happyandjust.nameless.devqol.inHypixel
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.utils.ScoreboardUtils

object Hypixel {
    var currentGame: GameType? = null
    val currentProperty = hashMapOf<PropertyKey, Any>()
    var inLobby = false
    var locrawInfo: LocrawInfo? = null

    fun <T> getProperty(key: PropertyKey): T {
        val value = currentProperty[key] ?: return key.defaultValue as T

        return value as T
    }

    fun updateGame() {

        currentProperty.clear()
        currentGame = null
        inLobby = false

        if (mc.thePlayer?.inHypixel() != true) return

        val locraw = locrawInfo ?: return

        if (locraw.mode == "lobby") {
            inLobby = true
            return
        }

        val type = locraw.gameType

        for (gameType in GameType.values()) {
            if (gameType.displayName == type) {
                val modeReqs = gameType.modeReqs

                if (modeReqs.isEmpty() || modeReqs.contains(locraw.mode)) {
                    currentGame = gameType
                }
                break
            }
        }

        when (currentGame) {
            GameType.MURDER_MYSTERY -> {
                currentProperty[PropertyKey.MURDERER_TYPE] = when (locraw.mode) {
                    "MURDER_INFECTION" -> MurdererMode.INFECTION
                    "MURDER_CLASSIC", "MURDER_DOUBLE_UP" -> MurdererMode.CLASSIC
                    "MURDER_ASSASSINS" -> MurdererMode.ASSASSIN
                    else -> MurdererMode.CLASSIC // wtf
                }
            }
            GameType.SKYBLOCK -> {
                currentProperty[PropertyKey.DUNGEON] = locraw.mode == "dungeon"
                currentProperty[PropertyKey.ISLAND] = locraw.mode
            }
            GameType.PARTY_GAMES -> {
                detect@ for (scoreboard in ScoreboardUtils.getSidebarLines(true)) {
                    for (partyGameType in PartyGamesType.values()) {
                        if (scoreboard.contains(partyGameType.scoreboardName ?: continue, true)) {
                            currentProperty[PropertyKey.PARTY_GAME_TYPE] = partyGameType
                            break@detect
                        }
                    }
                }
            }
        }

    }
}
