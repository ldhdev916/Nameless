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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.ScoreboardUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.util.regex.Pattern

object HighGroundProcessor : Processor(), ClientTickListener, StencilListener {

    private val SCOREBOARD_PATTERN = Pattern.compile("(?<name>\\w+): (?<score>\\d+)")
    var entityColor = { -1 }
    private val higherPlayers = hashSetOf<EntityPlayer>()
    private var scanTick = 0

    override fun tick() {
        scanTick = (scanTick + 1) % 10
        if (scanTick == 0) {
            higherPlayers.clear()

            val playersInScoreboard = ScoreboardUtils.getSidebarLines(true)
                .map { SCOREBOARD_PATTERN.matcher(it) }
                .filter { it.matches() }
                .map { it.group("name") to it.group("score").toInt() }
                .toMutableList()
            val myScore = playersInScoreboard.find { it.first == mc.thePlayer.name } ?: return // weird

            higherPlayers.addAll((playersInScoreboard - myScore).filter { it.second > myScore.second }
                .mapNotNull { mc.theWorld.getPlayerEntityByName(it.first) })
        }
    }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        return if (higherPlayers.contains(entity)) ColorInfo(entityColor(), ColorInfo.ColorPriority.HIGH) else null
    }
}