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

import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.elementa.utils.withAlpha
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBeacon
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

object FeaturePixelPartyHelper : SimpleFeature(Category.QOL, "pixelpartyhelper", "Pixel Party Helper", ""),
    ClientTickListener, WorldRenderListener, RenderOverlayListener, ServerChangeListener {

    init {
        parameters["boxcolor"] = FeatureParameter(
            0,
            "pixelparty",
            "boxcolor",
            "Box Color",
            "",
            Color.red.withAlpha(64).toChromaColor(),
            CChromaColor
        )
        parameters["beacon"] = FeatureParameter(
            1,
            "pixelparty",
            "beaconcolor",
            "Beacon Color",
            "",
            Color.blue.withAlpha(0.7f).toChromaColor(),
            CChromaColor
        )
        parameters["arrow"] = FeatureParameter(
            2,
            "pixelparty",
            "beaconarrow",
            "Show Direction Arrow to Beacon",
            "",
            true,
            CBoolean
        )
        parameters["safe"] = FeatureParameter(
            3,
            "pixelparty",
            "findsafe",
            "Find Safe Position",
            "Find position where distance to all kinds of blocks are nearly same\nSo you can go anywhere fast",
            false,
            CBoolean
        )
    }

    private var scanTick = 0

    private val from = BlockPos(-32, 0, 31)
    private val to = BlockPos(31, 0, -32)

    private var sameBlocks = emptySet<AxisAlignedBB>()
    private var beaconPosition: Vec3? = null
    private var safePosition = listOf<Pair<BlockPos, DistanceResult>>()
    private var shouldScanAgain = false

    private val getColorByStandardDeviationDiff: (Double) -> Int = {
        when (it) {
            in 0.0..0.5 -> Color.green
            in 0.5..3.0 -> Color.pink
            in 3.0..6.0 -> Color.orange
            else -> Color.red
        }.rgb
    }

    private fun checkForRequirement() = enabled && Hypixel.currentGame == GameType.PIXEL_PARTY

    @OptIn(DelicateCoroutinesApi::class)
    override fun tick() {
        if (!checkForRequirement()) return
        scanTick = (scanTick + 1) % 3

        if (scanTick != 0) return

        val set = hashSetOf<AxisAlignedBB>()

        mc.thePlayer.inventory.getStackInSlot(8)
            ?.takeIf { it.item == Item.getItemFromBlock(Blocks.stained_hardened_clay) }?.let {
                shouldScanAgain = true
                safePosition = emptyList()

                val meta = it.metadata

                for (pos in BlockPos.getAllInBox(from, to)) {
                    val blockState = mc.theWorld.getBlockState(pos)
                    val block = blockState.block.takeIf { block -> block == Blocks.stained_hardened_clay } ?: continue

                    if (block.getMetaFromState(blockState) == meta) {
                        set.add(pos.getAxisAlignedBB())
                    }
                }
            } ?: run {
            if (!shouldScanAgain || !getParameterValue<Boolean>("safe")) return@run
            val current = BlockPos(mc.thePlayer)
            if (current.x !in from.x..to.x || current.z !in to.z..from.z) return@run

            val allInBox = BlockPos.getAllInBox(from, to)
            if (allInBox.map { mc.theWorld.getBlockAtPos(it) }.filterIsInstance<BlockAir>().isEmpty()) {
                shouldScanAgain = false

                GlobalScope.launch {
                    val blockByMetadata = allInBox.groupBy {
                        val blockState = mc.theWorld.getBlockState(it)
                        blockState.block.getMetaFromState(blockState)
                    }

                    val getSortedByDistance: (BlockPos) -> List<BlockPos> = {
                        blockByMetadata.values.map { list -> list.sortedBy(it::distanceSq)[0] }
                    }

                    val comparator = compareBy<Pair<BlockPos, DistanceResult>>({ it.second.standardDeviation },
                        { it.second.averageDist }, { current.distanceSq(it.first) })

                    val pairs = arrayListOf<Pair<BlockPos, DistanceResult>>()

                    for (pos in allInBox.groupBy { it.x / 3 to it.z / 3 }.values.map { it.random() }) {

                        val sortedList = getSortedByDistance(pos)

                        val sortedDistanceList = sortedList.map { pos.distanceSq(it) }

                        val averageDist = sortedDistanceList.average()

                        val deviation = sortedDistanceList.sumOf { (it - averageDist).pow(2) }

                        val variance = deviation / sortedDistanceList.size

                        pairs.add(pos to DistanceResult(sortedList, averageDist, sqrt(variance)))

                    }

                    val preSet = pairs.sortedWith(comparator)

                    val first = preSet[0]

                    var lastDist = current.distanceSq(first.first)
                    val standardDeviation = first.second.standardDeviation

                    safePosition = preSet
                        .filter { it === first || it.second.standardDeviation - standardDeviation < 10 }
                        .filter {
                            if (it === first) return@filter true

                            val dist = current.distanceSq(it.first)

                            if (dist < lastDist) {
                                lastDist = dist
                                true
                            } else false
                        }
                }
            }
        }
        sameBlocks = set
        beaconPosition =
            BlockPos.getAllInBox(from.up(), to.up())
                .find { mc.theWorld.getBlockAtPos(it) is BlockBeacon }
                ?.toVec3()
    }

    override fun renderWorld(partialTicks: Float) {
        if (!checkForRequirement()) return

        val boxColor = getParameterValue<Color>("boxcolor").rgb

        sameBlocks.forEach { RenderUtils.drawBox(it, boxColor, partialTicks) }

        if (safePosition.isNotEmpty()) {
            val firstStandardDeviation = safePosition[0].second.standardDeviation
            safePosition.withIndex().forEach {

                val pos = it.value.first

                val diff = it.value.second.standardDeviation - firstStandardDeviation

                val rgb = getColorByStandardDeviationDiff(diff)

                RenderUtils.drawBox(pos.getAxisAlignedBB(), rgb and 0x40FFFFFF, partialTicks)
                RenderUtils.renderBeaconBeam(pos.toVec3(), rgb, 0.7F, partialTicks)

                RenderUtils.draw3DString(
                    "#${it.index + 1}",
                    pos.up(5),
                    2.0,
                    Color.red.rgb,
                    partialTicks
                )
            }
        }

        beaconPosition?.let {
            val color = getParameterValue<Color>("beacon").rgb
            RenderUtils.renderBeaconBeam(
                it,
                color,
                color.alpha / 255f,
                partialTicks
            )
        }
    }

    override fun renderOverlay(partialTicks: Float) {
        if (!checkForRequirement()) return

        beaconPosition?.let {
            if (getParameterValue("arrow")) {
                RenderUtils.drawDirectionArrow(it, Color.red.rgb)
            }
        }
    }

    data class DistanceResult(val posList: List<BlockPos>, val averageDist: Double, val standardDeviation: Double)

    override fun onServerChange(server: String) {
        shouldScanAgain = true
    }
}