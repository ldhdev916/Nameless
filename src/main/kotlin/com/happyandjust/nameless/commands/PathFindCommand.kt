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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.dsl.drawPaths
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.sendDebugMessage
import com.happyandjust.nameless.pathfinding.ModPathFinding
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.SubCommand
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.system.measureTimeMillis

object PathFindCommand : Command("pathfind") {

    private val paths = CopyOnWriteArrayList<BlockPos>()

    init {
        on<RenderWorldLastEvent>().subscribe {
            paths.drawPaths(Color.red.rgb, partialTicks)
        }
    }

    @DefaultHandler
    fun handle(x: Int, y: Int, z: Int, timeout: Int?) {
        CoroutineScope(Dispatchers.Default).launch {
            paths.clear()
            val time = measureTimeMillis {
                paths.addAll(ModPathFinding(BlockPos(x, y, z), true, (timeout ?: 10) * 1000L).findPath())
            }
            sendDebugMessage("Path Finding took §a${time / 1000.0}s")
        }
    }

    @SubCommand("clear")
    fun clearPath() = paths.clear()
}