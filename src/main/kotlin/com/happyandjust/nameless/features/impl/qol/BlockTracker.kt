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

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.elementa.utils.withAlpha
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.block.Block
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

@OptIn(DelicateCoroutinesApi::class)
object BlockTracker : SimpleFeature("blockTracker", "Block Tracker", "") {

    private val scanBlocks = hashSetOf<Block>()
    private val pathBlocks = hashSetOf<Block>()

    init {
        parameter(Unit) {
            matchKeyCategory()
            key = "blocks"
            title = "Blocks to Scan"

            componentType = null

            settings {
                ordinal = -1
            }

            for (block in Block.blockRegistry) {
                parameter(false) {
                    matchKeyCategory()
                    key = block.registryName
                    title = block.displayName

                    settings {
                        ordinal = if (value) {
                            scanBlocks.add(block)
                            0
                        } else 1

                        onValueChange {
                            ordinal = if (it) {
                                scanBlocks.add(block)
                                0
                            } else {
                                scanBlocks.remove(block)
                                1
                            }
                        }
                    }

                    parameter(Color.white.withAlpha(0.4f).toChromaColor()) {
                        matchKeyCategory()
                        key = "${block.registryName}_box_color"
                        title = "Box Color"
                    }
                }
            }
        }

        parameter(50) {
            matchKeyCategory()
            key = "scanRadius"
            title = "Scan Radius"

            settings {
                ordinal = 2
                minValueInt = 5
                maxValueInt = 150
            }
        }

        parameter(false) {
            matchKeyCategory()
            key = "showPath"
            title = "Show Path to Nearest Block"

            settings {
                ordinal = 3
            }

            for (block in Block.blockRegistry) {
                parameter(true) {
                    matchKeyCategory()
                    key = "${block.registryName}_path"
                    title = block.displayName

                    settings {
                        ordinal = if (value) {
                            pathBlocks.add(block)
                            0
                        } else 1

                        onValueChange {
                            ordinal = if (it) {
                                pathBlocks.add(block)
                                0
                            } else {
                                pathBlocks.remove(block)
                                1
                            }
                        }
                    }

                    parameter(Color.red.toChromaColor()) {
                        matchKeyCategory()
                        key = "${block.registryName}_path_color"
                        title = "Path Color"
                    }
                }
            }

            parameter(true) {
                matchKeyCategory()
                key = "canFly"
                title = "Can Fly"
                desc = "Whether you can fly or not when path finding"

                settings {
                    ordinal = -2
                }
            }

            parameter(true) {
                matchKeyCategory()
                key = "showBeacon"
                title = "Show Beacon"
                desc = "Render beacon at nearest block position"

                settings {
                    ordinal = -1
                }
            }
        }
    }


    private val scanTick = TickTimer.withSecond(1.5)

    private var paths = listOf<PathTriple>()
    private var boxColors = listOf<BoxPair>()

    init {
        on<SpecialTickEvent>().filter { enabled && scanTick.update().check() }.subscribe {
            GlobalScope.launch {
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
                    getParameterValue("blocks/${it.registryName}/${it.registryName}_box_color")
                }

                val pathColor: (Block) -> Color = {
                    getParameterValue("showPath/${it.registryName}_path/${it.registryName}_path_color")
                }

                boxColors = blocks.flatMap { (block, posList) ->
                    posList.map {
                        it.getAxisAlignedBB() to boxColor(block)
                    }
                }

                paths = blocksForPaths.map { (block, posList) ->
                    val near = posList.minByOrNull(mc.thePlayer::getDistanceSq)!!

                    Triple(
                        async { ModPathFinding(near, showPath_canFly).findPath() },
                        pathColor(block),
                        near
                    )
                }.map { (deferred, color, near) -> Triple(deferred.await(), color, near) }
            }
        }

        on<RenderWorldLastEvent>().filter { enabled }.subscribe {
            for ((aabb, color) in boxColors) {
                RenderUtils.drawBox(aabb, color.rgb, partialTicks)
            }

            if (showPath) {
                for ((paths, color, destination) in paths) {
                    RenderUtils.drawPath(paths, color.rgb, partialTicks)
                    if (showPath_showBeacon) {
                        RenderUtils.renderBeaconBeam(destination.toVec3(), color.rgb, 0.7f, partialTicks)
                    }
                }
            }
        }
    }
}

typealias PathTriple = Triple<List<BlockPos>, Color, BlockPos>

typealias BoxPair = Pair<AxisAlignedBB, Color>