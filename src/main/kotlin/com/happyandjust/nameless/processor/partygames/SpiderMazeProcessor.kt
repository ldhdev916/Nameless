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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.PartyGameChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.util.BlockPos
import java.awt.Color
import java.util.concurrent.Executors

object SpiderMazeProcessor : Processor(), ClientTickListener, WorldRenderListener, PartyGameChangeListener {

    private var mazePath = emptyList<BlockPos>()
    private val mazeEnds =
        listOf(BlockPos(45, 2, 2099), BlockPos(44, 2, 2098), BlockPos(45, 2, 2098), BlockPos(44, 2, 2099))
    private val threadPool = Executors.newFixedThreadPool(2)
    private var pathTick = 0


    override fun tick() {
        pathTick = (pathTick + 1) % 20

        if (pathTick == 0) {
            val nearestEnd = mazeEnds.sortedBy { mc.thePlayer.getDistanceSq(it) }[0]

            threadPool.execute { mazePath = ModPathFinding(nearestEnd, false).findPath().get() }
        }
    }

    override fun renderWorld(partialTicks: Float) {
        if (mazePath.isNotEmpty()) {
            RenderUtils.drawPath(mazePath, Color.red.rgb, partialTicks)
        }
    }

    override fun onPartyGameChange(from: PartyGamesType?, to: PartyGamesType?) {
        if (from == PartyGamesType.SPIDER_MAZE || to == PartyGamesType.SPIDER_MAZE) {
            mazePath = emptyList()
        }
    }
}