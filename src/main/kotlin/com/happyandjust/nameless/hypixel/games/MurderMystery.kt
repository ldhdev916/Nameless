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

import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.features.impl.qol.MurdererFinder
import com.happyandjust.nameless.hypixel.LocrawInfo
import com.happyandjust.nameless.hypixel.murderer.Assassins
import com.happyandjust.nameless.hypixel.murderer.Classic
import com.happyandjust.nameless.hypixel.murderer.Infection
import com.happyandjust.nameless.hypixel.murderer.MurdererMode

class MurderMystery : GameType {

    var murdererMode: MurdererMode? = null
        private set(value) {
            field?.unregisterAll()
            field = value
            if (value != null && MurdererFinder.enabled && value.isEnabled()) {
                value.registerEventListeners()
            }
        }

    override fun handleProperty(locrawInfo: LocrawInfo) {
        murdererMode = murdererModes.find { locrawInfo.mode in it.modes }?.createImpl()
    }

    override fun printProperties() {
        sendPrefixMessage("Murderer Mode: $murdererMode")
    }

    companion object : GameTypeCreator {

        private val murdererModes = setOf(Classic, Infection, Assassins)

        override fun isCurrent(locrawInfo: LocrawInfo) = locrawInfo.gameType == "MURDER_MYSTERY"

        override fun createGameTypeImpl() = MurderMystery()
    }
}