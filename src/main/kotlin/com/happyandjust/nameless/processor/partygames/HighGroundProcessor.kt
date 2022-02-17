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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.highGround_color
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.ScoreboardUtils
import net.minecraft.entity.player.EntityPlayer

object HighGroundProcessor : Processor() {

    private val SCOREBOARD_PATTERN = "(?<name>\\w+): (?<score>\\d+)".toPattern()
    private val higherPlayers = hashSetOf<EntityPlayer>()
    private val scanTimer = TickTimer.withSecond(0.5)
    override val filter = PartyGamesHelper.getFilter(this)

    init {
        request<SpecialTickEvent>().filter { scanTimer.update().check() }.subscribe {
            higherPlayers.clear()

            val playersInScoreboard = ScoreboardUtils.getSidebarLines(true)
                .map { SCOREBOARD_PATTERN.matcher(it) }
                .filter { it.matches() }
                .map { it.group("name") to it.group("score").toInt() }
                .toMutableList()
            val myScore = playersInScoreboard.find { it.first == mc.thePlayer.name } ?: return@subscribe // weird

            higherPlayers.addAll((playersInScoreboard - myScore).filter { it.second > myScore.second }
                .mapNotNull { mc.theWorld.getPlayerEntityByName(it.first) })
        }

        request<OutlineRenderEvent>().filter { entity in higherPlayers }.subscribe {
            colorInfo = ColorInfo(PartyGamesHelper.highGround_color.rgb, ColorInfo.ColorPriority.HIGH)
        }
    }
}