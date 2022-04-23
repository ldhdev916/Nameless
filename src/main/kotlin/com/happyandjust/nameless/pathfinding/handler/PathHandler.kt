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

package com.happyandjust.nameless.pathfinding.handler

import com.happyandjust.nameless.pathfinding.ModPathFinding
import net.minecraft.util.BlockPos

interface PathHandler {

    fun getPath(destination: BlockPos): List<BlockPos>
}

class PathHandlerImpl(
    var canFly: Boolean,
    var timeout: Long = 300,
    var cacheBlock: Boolean = true,
    var additionalValidCheck: (BlockPos) -> Boolean = { true }
) : PathHandler {

    override fun getPath(destination: BlockPos): List<BlockPos> {
        return ModPathFinding(destination, canFly, timeout, cacheBlock, additionalValidCheck).findPath()
    }
}