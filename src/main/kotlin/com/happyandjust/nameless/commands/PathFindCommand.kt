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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.SubCommand
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import java.util.*
import kotlin.concurrent.thread

object PathFindCommand : Command("pathfind") {

    private val paths = Collections.synchronizedList(arrayListOf<BlockPos>())

    init {
        on<RenderWorldLastEvent>().subscribe {
            synchronized(paths) {
                RenderUtils.drawPath(paths, Color.red.rgb, partialTicks)
            }
        }
    }

    @DefaultHandler
    fun handle(x: Int, y: Int, z: Int, timeout: Int?) {
        thread {
            paths.clear()
            paths.addAll(ModPathFinding(BlockPos(x, y, z), true, (timeout ?: 10) * 1000L).findPath())
        }
    }

    @SubCommand("clear")
    fun clearPath() = paths.clear()
}