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

package com.happyandjust.nameless.pathfinding

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.utils.Utils
import net.minecraft.pathfinding.PathFinder
import net.minecraft.util.BlockPos
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class ModPathFinding(private val target: BlockPos, private val canFly: Boolean) {

    private val nodeProcessorPath = NodeProcessorPath(canFly)
    private val pathFinder = PathFinder(nodeProcessorPath)
    private val async = Executors.newScheduledThreadPool(2)


    fun findPath(onArrive: () -> Unit = {}): ScheduledFuture<List<BlockPos>> {
        return async.schedule(
            Callable {
                val list = arrayListOf<BlockPos>()

                val latest =
                    pathFinder.createEntityPathTo(
                        mc.theWorld,
                        mc.thePlayer,
                        if (!canFly) Utils.getHighestGround(target, false) else target,
                        Int.MAX_VALUE.toFloat()
                    )
                        ?: return@Callable list
                list.addAll((0 until latest.currentPathLength).map {
                    val pathPoint = latest.getPathPointFromIndex(it)
                    BlockPos(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord)
                })

                onArrive()
                list
            }, 0, TimeUnit.MICROSECONDS
        )
    }
}
