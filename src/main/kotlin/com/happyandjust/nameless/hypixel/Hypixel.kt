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

package com.happyandjust.nameless.hypixel

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.sendDebugMessage
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.hypixel.games.*
import gg.essential.api.EssentialAPI
import net.minecraftforge.common.MinecraftForge

object Hypixel {
    private val gameTypeCreators =
        setOf(BedWars, GrinchSimulator, GuessTheBuild, Lobby, MurderMystery, PartyGames, PixelParty, SkyBlock, SkyWars)
    var currentGame: GameType? = null
        private set
    var locrawInfo: LocrawInfo? = null
    private var prevServer: String? = null

    private val updateTimer = TickTimer.withSecond(2)

    init {
        on<SpecialTickEvent>().filter { updateTimer.update().check() }.subscribe {
            updateGame()
        }
    }

    fun updateGame() {
        if (!EssentialAPI.getMinecraftUtil().isHypixel()) {
            currentGame = null
            return
        }

        val locraw = locrawInfo ?: run {
            currentGame = null
            return
        }

        handleServerChange(locraw)
        currentGame?.handleProperty(locraw)
    }

    private fun handleServerChange(locraw: LocrawInfo) {
        val server = locraw.server
        if (prevServer != server) {

            currentGame = gameTypeCreators.find { it.isCurrent(locraw) }?.createGameTypeImpl()
            sendDebugMessage("Hypixel", "Current Game: $currentGame")

            MinecraftForge.EVENT_BUS.post(HypixelServerChangeEvent(server))
        }
        prevServer = server
    }
}