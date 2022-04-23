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

import net.minecraft.util.BlockPos

class CachePathHandler(private val base: PathHandler, var cacheSecond: Number) : PathHandler {

    private var lastCachedTime = -1L
    private var cached: List<BlockPos>? = null
        get() = field?.takeIf { (System.currentTimeMillis() - lastCachedTime) <= (cacheSecond.toDouble() * 1000) }
        set(value) {
            field = value
            lastCachedTime = System.currentTimeMillis()
        }

    override fun getPath(destination: BlockPos): List<BlockPos> {
        return cached ?: base.getPath(destination).also { cached = it }
    }
}