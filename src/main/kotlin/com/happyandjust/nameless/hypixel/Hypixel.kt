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
import com.happyandjust.nameless.devqol.matchesMatcher
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.hypixel.skyblock.DungeonFloor
import com.happyandjust.nameless.utils.ScoreboardUtils
import net.minecraftforge.common.MinecraftForge
import java.util.regex.Pattern

object Hypixel {
    private val DUNGEONS_FLOOR_PATTERN = Pattern.compile("The Catacombs \\((?<name>([FM][1-7]|E))\\)")
    var currentGame: GameType? = null
    val currentProperty = hashMapOf<PropertyKey, Any>()
    var inLobby = false
    var locrawInfo: LocrawInfo? = null
    private var prevServer: String? = null

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

        var serverChanged = false

        val server = locraw.server
        if (prevServer != server) {
            serverChanged = true
        }
        prevServer = server

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

                detect@ for (scoreboard in ScoreboardUtils.getSidebarLines(true)) {
                    DUNGEONS_FLOOR_PATTERN.matchesMatcher(scoreboard.trim()) {
                        currentProperty[PropertyKey.DUNGEON_FLOOR] =
                            DungeonFloor.getByScoreboardName(it.group("name")) ?: return@matchesMatcher
                    }
                }
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

        if (serverChanged) {
            MinecraftForge.EVENT_BUS.post(HypixelServerChangeEvent(server))
            // to notify game changes at the end, we post event
        }

    }
}
