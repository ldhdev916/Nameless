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

package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.getSidebarLines
import com.happyandjust.nameless.dsl.matchesMatcher
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.entity.player.EntityPlayer

class HighGround : PartyMiniGames {

    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val scanTimer = TickTimer.withSecond(0.5)
    private val higherPlayers = hashSetOf<EntityPlayer>()

    override fun isEnabled() = PartyGamesHelper.highGround

    override fun registerEventListeners() {
        on<SpecialTickEvent>().timerFilter(scanTimer).addSubscribe {
            val playersInScoreboard = buildList {
                for (line in mc.theWorld.getSidebarLines()) {
                    SCOREBOARD_PATTERN.matchesMatcher(line) {
                        val playerName = group("name")
                        val score = group("score").toInt()

                        add(playerName to score)
                    }
                }
            }.sortedByDescending { it.second }.map { it.first }

            higherPlayers.clear()
            higherPlayers.addAll(
                playersInScoreboard.takeWhile { it != mc.thePlayer.name }
                    .mapNotNull { mc.theWorld.getPlayerEntityByName(it) }
            )
        }

        on<OutlineRenderEvent>().filter { entity in higherPlayers }.addSubscribe {
            colorInfo = ColorInfo(outlineColor, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : PartyMiniGamesCreator {

        private val outlineColor
            get() = PartyGamesHelper.highGroundColor.rgb
        private val SCOREBOARD_PATTERN = "(?<name>\\w+): (?<score>\\d+)".toPattern()

        override fun createImpl() = HighGround()

        override val scoreboardIdentifier = "High Ground"
    }
}