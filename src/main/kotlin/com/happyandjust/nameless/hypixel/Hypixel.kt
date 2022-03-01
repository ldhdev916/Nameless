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

import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.hypixel.games.*
import gg.essential.api.EssentialAPI
import net.minecraftforge.common.MinecraftForge

object Hypixel {
    private val gameTypeFactories =
        setOf(BedWars, GrinchSimulator, GuessTheBuild, Lobby, MurderMystery, PartyGames, PixelParty, SkyBlock, SkyWars)
    var currentGame: GameType? = null
    var locrawInfo: LocrawInfo? = null
    private var prevServer: String? = null

    fun updateGame() {
        currentGame = null
        if (!EssentialAPI.getMinecraftUtil().isHypixel()) return

        val locraw = locrawInfo ?: return

        currentGame = gameTypeFactories.map { it.createGameTypeImpl() }.find { it.isCurrent(locraw) }
        currentGame?.handleProperty(locraw)

        handleServerChange(locraw)
    }

    private fun handleServerChange(locraw: LocrawInfo) {
        var serverChanged = false

        val server = locraw.server
        if (prevServer != server) {
            serverChanged = true
        }
        prevServer = server

        if (serverChanged) {
            MinecraftForge.EVENT_BUS.post(HypixelServerChangeEvent(server))
        }
    }
}