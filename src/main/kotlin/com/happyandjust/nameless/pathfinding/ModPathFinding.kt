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

package com.happyandjust.nameless.pathfinding

import com.happyandjust.nameless.dsl.mc
import net.minecraft.pathfinding.PathFinder
import net.minecraft.util.BlockPos

class ModPathFinding(
    private val target: BlockPos,
    canFly: Boolean,
    timeout: Long = 300,
    cache: Boolean = true,
    additionalValidCheck: (BlockPos) -> Boolean = { true }
) {

    private val nodeProcessorPath = NodeProcessorPath(canFly, timeout, cache, additionalValidCheck)
    private val pathFinder = PathFinder(nodeProcessorPath)

    fun findPath(): List<BlockPos> {
        val list = arrayListOf<BlockPos>()

        val latest = pathFinder.createEntityPathTo(
            mc.theWorld,
            mc.thePlayer,
            target,
            Int.MAX_VALUE.toFloat()
        ) ?: return emptyList()

        list.addAll(List(latest.currentPathLength) {
            val pathPoint = latest.getPathPointFromIndex(it)
            BlockPos(pathPoint.xCoord, pathPoint.yCoord, pathPoint.zCoord)
        })

        return list
    }

}