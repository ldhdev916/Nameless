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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.BlockSerializer
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.pathfinding.ModPathFinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.block.Block
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

object BlockTracker : SimpleFeature("blockTracker", "Block Tracker", "") {

    init {
        hierarchy {
            +::scanBlocks

            +::scanRadius

            ::showPath {
                +::canFly

                +::showBeacon

                +::pathBlocks
            }
        }
    }

    private var scanRadius by parameter(50) {
        key = "scanRadius"
        title = "Scan Radius"

        settings {
            minValueInt = 5
            maxValueInt = 150
        }
    }

    private var showPath by parameter(false) {
        key = "showPath"
        title = "Show Path to Nearest Block"
    }

    private var canFly by parameter(true) {
        key = "canFly"
        title = "Can Fly"
        desc = "Whether you can fly or not when path finding"
    }

    private var showBeacon by parameter(true) {
        matchKeyCategory()
        key = "showBeacon"
        title = "Show Beacon"
        desc = "Render beacon at nearest block position"
    }

    private var pathBlocks by parameter(Block.blockRegistry.toList(), serializer = ListSerializer(BlockSerializer)) {
        key = "pathBlocks"
        title = "Blocks showing path"

        settings {
            listSerializer { it.displayName }
            allValueList = { Block.blockRegistry.sortedWith(compareBy({ it.displayName }, { it in value })) }
        }
    }

    private var scanBlocks by parameter(emptyList<Block>(), serializer = ListSerializer(BlockSerializer)) {
        key = "scanBlocks"
        title = "Blocks to Track"

        settings {
            listSerializer { it.displayName }
            allValueList = { Block.blockRegistry.sortedWith(compareBy({ it.displayName }, { it in value })) }
        }
    }

    private val scanTick = TickTimer.withSecond(1.5)

    private var paths = listOf<PathTriple>()
    private var boxColors = listOf<BoxPair>()

    init {
        val scope = CoroutineScope(Dispatchers.Default)
        on<SpecialTickEvent>().filter { enabled && scanTick.update().check() }.subscribe {
            scope.launch {
                val current = BlockPos(mc.thePlayer)

                val from = BlockPos(current.x - scanRadius, max(current.y - scanRadius, 0), current.z - scanRadius)
                val to = BlockPos(current.x + scanRadius, min(current.y + scanRadius, 255), current.z + scanRadius)

                val blocks = BlockPos.getAllInBox(from, to)
                    .asSequence()
                    .filter { mc.theWorld.isBlockLoaded(it, false) }
                    .groupBy { mc.theWorld.getBlockAtPos(it) }
                    .filterKeys { it in scanBlocks }

                val blocksForPaths = blocks.filterKeys { it in pathBlocks }

                val boxColor: (Block) -> Color = {
                    // TODO Set color
                    Color.magenta
                }

                val pathColor: (Block) -> Color = {
                    // TODO Set color
                    Color.magenta
                }

                boxColors = blocks.flatMap { (block, posList) ->
                    posList.map {
                        it.getAxisAlignedBB() to boxColor(block)
                    }
                }

                paths = blocksForPaths.map { (block, posList) ->
                    val near = posList.minByOrNull(mc.thePlayer::getDistanceSq)!!

                    Triple(
                        async { ModPathFinding(near, canFly).findPath() },
                        pathColor(block),
                        near
                    )
                }.map { (deferred, color, near) -> Triple(deferred.await(), color, near) }
            }
        }

        on<RenderWorldLastEvent>().filter { enabled }.subscribe {
            for ((aabb, color) in boxColors) {
                aabb.drawFilledBox(color.rgb, partialTicks)
            }

            if (showPath) {
                for ((paths, color, destination) in paths) {
                    paths.drawPaths(color.rgb, partialTicks)
                    if (showBeacon) {
                        destination.toVec3().renderBeaconBeam(color.rgb, 0.7f, partialTicks)
                    }
                }
            }
        }
    }
}

typealias PathTriple = Triple<List<BlockPos>, Color, BlockPos>

typealias BoxPair = Pair<AxisAlignedBB, Color>