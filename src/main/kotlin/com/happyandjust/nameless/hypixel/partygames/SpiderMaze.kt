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
import com.happyandjust.nameless.dsl.drawPaths
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.pathfinding.ModPathFinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.block.BlockWeb
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList

class SpiderMaze : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()
    private val pathTimer = TickTimer.withSecond(1.5)
    private val pathScope = CoroutineScope(Dispatchers.Default)

    private val mazePaths = CopyOnWriteArrayList<BlockPos>()

    override fun isEnabled() = PartyGamesHelper.maze

    override fun registerEventListeners() {
        on<SpecialTickEvent>().timerFilter(pathTimer).addSubscribe {
            pathScope.launch {
                val paths = mazeEnds.map {
                    async {
                        doFindPath(it)
                    }
                }.map { it.await() }

                mazePaths.clear()
                mazePaths.addAll(paths.minByOrNull { it.size }!!)
            }
        }

        on<RenderWorldLastEvent>().addSubscribe {
            mazePaths.drawPaths(Color.red.rgb, partialTicks)
        }
    }

    private fun doFindPath(pos: BlockPos) = ModPathFinding(pos, false, additionalValidCheck = {
        mc.theWorld.getBlockAtPos(it) !is BlockWeb
    }).findPath()

    companion object {
        private val mazeEnds =
            setOf(BlockPos(45, 2, 2099), BlockPos(44, 2, 2098), BlockPos(45, 2, 2098), BlockPos(44, 2, 2099))
    }
}