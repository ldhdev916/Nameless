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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.serialization.converters.CString
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
object BlockTracker : SimpleFeature(Category.QOL, "blocktracker", "Block Tracker", "") {

    private val scanBlocks = hashSetOf<Block>()
    private val pathBlocks = hashSetOf<Block>()

    private var blocks by object : FeatureParameter<String>(
        -1,
        "blocktracker",
        "blocks",
        "Blocks to Scan",
        "",
        "",
        CString
    ) {
        init {
            for (block in Block.blockRegistry) {
                parameters[block.registryName] = FeatureParameter(
                    1,
                    "blocktracker",
                    block.registryName,
                    block.displayName,
                    "",
                    false,
                    CBoolean
                ).apply {
                    if (value) {
                        ordinal = 0
                        scanBlocks.add(block)
                    }

                    onValueChange = {
                        ordinal = if (it) {
                            scanBlocks.add(block)
                            0
                        } else {
                            scanBlocks.remove(block)
                            1
                        }
                    }

                    parameters["color"] = FeatureParameter(
                        0,
                        "blocktracker",
                        "${block.registryName}_box_color",
                        "Box Color",
                        "",
                        Color.white.withAlpha(0.4f).toChromaColor(),
                        CChromaColor
                    )
                }
            }
        }

        override fun getComponentType(): ComponentType? = null
    }

    private var scanRadius by FeatureParameter(2, "blocktracker", "radius", "Scan Radius", "", 50, CInt).apply {
        minValue = 5.0
        maxValue = 150.0
    }

    private var showPath by FeatureParameter(
        3,
        "blocktracker",
        "showpath",
        "Show Path to Nearest Block",
        "",
        false,
        CBoolean
    ).apply {
        for (block in Block.blockRegistry) {
            parameters[block.registryName] =
                FeatureParameter(
                    1,
                    "blocktracker",
                    "${block.registryName}_path",
                    block.displayName,
                    "",
                    true,
                    CBoolean
                ).apply {
                    if (value) {
                        pathBlocks.add(block)
                        ordinal = 0
                    }

                    onValueChange = {
                        ordinal = if (it) {
                            pathBlocks.add(block)
                            0
                        } else {
                            pathBlocks.remove(block)
                            1
                        }
                    }

                    parameters["color"] = FeatureParameter(
                        0,
                        "blocktracker",
                        "${block.registryName}_path_color",
                        "Path Color",
                        "",
                        Color.red.toChromaColor(),
                        CChromaColor
                    )
                }
        }
    }

    @SubParameterOf("showPath")
    private var canFly by FeatureParameter(
        -2,
        "blocktracker",
        "canfly",
        "Can Fly",
        "Whether you can fly or not when path finding",
        true,
        CBoolean
    )

    @SubParameterOf("showPath")
    private var showBeacon by FeatureParameter(
        -1,
        "blocktracker",
        "showbeacon",
        "Show Beacon",
        "Render beacon at nearest block position",
        true,
        CBoolean
    )

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
                    getParameter<String>("blocks").getParameter<Boolean>(it.registryName)
                        .getParameterValue<Color>("color")
                }

                val pathColor: (Block) -> Color = {
                    getParameter<Boolean>("showPath").getParameter<Boolean>(it.registryName)
                        .getParameterValue<Color>("color")
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
                RenderUtils.drawBox(aabb, color.rgb, partialTicks)
            }

            if (showPath) {
                for ((paths, color, destination) in paths) {
                    RenderUtils.drawPath(paths, color.rgb, partialTicks)
                    if (showBeacon) {
                        RenderUtils.renderBeaconBeam(destination.toVec3(), color.rgb, 0.7f, partialTicks)
                    }
                }
            }
        }
    }
}

typealias PathTriple = Triple<List<BlockPos>, Color, BlockPos>

typealias BoxPair = Pair<AxisAlignedBB, Color>