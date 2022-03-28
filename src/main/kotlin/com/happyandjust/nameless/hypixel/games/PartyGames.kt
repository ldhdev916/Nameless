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

import com.happyandjust.nameless.dsl.getSidebarLines
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.sendDebugMessage
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.hypixel.LocrawInfo
import com.happyandjust.nameless.hypixel.partygames.*

class PartyGames : GameType {

    var partyMiniGames: PartyMiniGames? = null
        private set(value) {
            field?.unregisterAll()
            field = value

            if (value != null && PartyGamesHelper.enabled && value.isEnabled()) {
                value.registerEventListeners()
            }
        }

    override fun handleProperty(locrawInfo: LocrawInfo) {
        for (scoreboard in mc.theWorld.getSidebarLines()) {
            val partyMiniGames = findPartyMiniGames(scoreboard)
            if (isNewMiniGames(partyMiniGames)) {
                this.partyMiniGames = partyMiniGames
                sendDebugMessage("Changed to $partyMiniGames")
                break
            }
        }
    }

    private fun findPartyMiniGames(scoreboard: String) =
        partyMiniGamesList.find { scoreboard.contains(it.scoreboardIdentifier, true) }?.createImpl()

    private fun isNewMiniGames(partyMiniGames: PartyMiniGames?) =
        partyMiniGames != null && this.partyMiniGames?.javaClass != partyMiniGames.javaClass

    override fun printProperties() {
        sendPrefixMessage("Party Games Type: ${partyMiniGames?.javaClass?.name}")
    }

    companion object : GameTypeCreator {
        private val partyMiniGamesList = setOf(
            AnimalSlaughter,
            AnvilSpleef,
            Avalanche,
            Dive,
            HighGround,
            JigsawRush,
            LabEscape,
            RPG16,
            SpiderMaze
        )

        override fun createGameTypeImpl() = PartyGames()

        override fun isCurrent(locrawInfo: LocrawInfo) = locrawInfo.gameType == "ARCADE" && locrawInfo.mode == "PARTY"
    }
}