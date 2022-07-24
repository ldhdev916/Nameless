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
import com.happyandjust.nameless.hypixel.partygames.PartyMiniGames
import com.happyandjust.nameless.hypixel.partygames.PartyMiniGamesFactory

class PartyGames(private val factory: PartyMiniGamesFactory) : GameType {

    var partyMiniGames: PartyMiniGames? = null
        private set(value) {
            field?.unregisterAll()
            field = value

            if (value != null && PartyGamesHelper.enabled && value.isEnabled()) {
                value.registerEventListeners()
            }
        }

    override fun handleProperty(locrawInfo: LocrawInfo) {
        val foundPartyMiniGames = mc.theWorld.getSidebarLines().mapNotNull(factory::createPartyMiniGames).singleOrNull()
        if (isNewMiniGames(foundPartyMiniGames)) {
            partyMiniGames = foundPartyMiniGames
            sendDebugMessage("Changed to $foundPartyMiniGames")
        }
    }

    private fun isNewMiniGames(partyMiniGames: PartyMiniGames?) =
        this.partyMiniGames?.javaClass != partyMiniGames?.javaClass

    override fun printProperties() {
        sendPrefixMessage("Party Games Type: ${partyMiniGames?.javaClass?.name}")
    }
}