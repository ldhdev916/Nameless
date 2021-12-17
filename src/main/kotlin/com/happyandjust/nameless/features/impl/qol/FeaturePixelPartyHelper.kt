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
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.elementa.utils.withAlpha
import kotlinx.coroutines.*
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockBeacon
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import kotlin.math.pow
import kotlin.math.sqrt

@OptIn(DelicateCoroutinesApi::class)
object FeaturePixelPartyHelper : SimpleFeature(Category.QOL, "pixelpartyhelper", "Pixel Party Helper", "") {

    private var boxColor by FeatureParameter(
        0,
        "pixelparty",
        "boxcolor",
        "Box Color",
        "",
        Color.red.withAlpha(64).toChromaColor(),
        CChromaColor
    )

    private var beacon by FeatureParameter(
        1,
        "pixelparty",
        "beaconcolor",
        "Beacon Color",
        "",
        Color.blue.withAlpha(0.7f).toChromaColor(),
        CChromaColor
    )

    private var arrow by FeatureParameter(
        2,
        "pixelparty",
        "beaconarrow",
        "Show Direction Arrow to Beacon",
        "",
        true,
        CBoolean
    )

    private var safe by FeatureParameter(
        3,
        "pixelparty",
        "findsafe",
        "Find Safe Position",
        "Find position where distance to all kinds of blocks are nearly same\nSo you can go anywhere fast",
        false,
        CBoolean
    )

    private var scanTimer = TickTimer(3)

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

    init {
        on<SpecialTickEvent>().filter { checkForRequirement() && scanTimer.update().check() }.subscribe {

            val set = hashSetOf<AxisAlignedBB>()

            mc.thePlayer.inventory.getStackInSlot(8)
                ?.takeIf { it.item == Item.getItemFromBlock(Blocks.stained_hardened_clay) }?.let {
                    shouldScanAgain = true
                    safePosition = emptyList()

                    val meta = it.metadata

                    for (pos in BlockPos.getAllInBox(from, to)) {
                        val blockState = mc.theWorld.getBlockState(pos)
                        val block =
                            blockState.block.takeIf { block -> block == Blocks.stained_hardened_clay } ?: continue

                        if (block.getMetaFromState(blockState) == meta) {
                            set.add(pos.getAxisAlignedBB())
                        }
                    }
                } ?: run {
                if (!shouldScanAgain || !safe) return@run
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

                        val pairs = arrayListOf<Deferred<Pair<BlockPos, DistanceResult>>>().apply {
                            for (pos in allInBox.groupBy { it.x / 2 to it.z / 2 }.values.map { it.random() }) {
                                add(async {
                                    val sortedList = getSortedByDistance(pos)

                                    val sortedDistanceList = sortedList.map { pos.distanceSq(it) }

                                    val averageDist = sortedDistanceList.average()

                                    val deviation = sortedDistanceList.sumOf { (it - averageDist).pow(2) }

                                    val variance = deviation / sortedDistanceList.size

                                    pos to DistanceResult(sortedList, averageDist, sqrt(variance))
                                })
                            }
                        }

                        val preSet = pairs.map { it.await() }.sortedWith(comparator)

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

        on<RenderWorldLastEvent>().filter { checkForRequirement() }.subscribe {
            sameBlocks.forEach { RenderUtils.drawBox(it, boxColor.rgb, partialTicks) }

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
                RenderUtils.renderBeaconBeam(
                    it,
                    beacon.rgb,
                    beacon.alpha / 255f,
                    partialTicks
                )
            }
        }

        on<SpecialOverlayEvent>().filter { checkForRequirement() && arrow }.subscribe {
            beaconPosition?.let {
                RenderUtils.drawDirectionArrow(it, Color.red.rgb)
            }
        }

        on<HypixelServerChangeEvent>().subscribe { shouldScanAgain = true }
    }

    data class DistanceResult(val posList: List<BlockPos>, val averageDist: Double, val standardDeviation: Double)
}