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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.contains
import com.happyandjust.nameless.dsl.getAxisAlignedBB
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.autoFillEnum
import com.happyandjust.nameless.features.base.listParameter
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.radius
import com.happyandjust.nameless.features.selectedGemstoneTypes
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.hypixel.skyblock.Gemstone
import com.happyandjust.nameless.utils.RenderUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import kotlin.math.max
import kotlin.math.min

@OptIn(DelicateCoroutinesApi::class)
object GemstoneESP : SimpleFeature(
    "gemstoneEsp",
    "Gemstone ESP",
    "Render box on gemstones in SkyBlock Crystal Hollows"
) {

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty<String>(PropertyKey.ISLAND) == "crystal_hollows"

    private val scanTimer = TickTimer.withSecond(2)
    private var gemstoneBlocks = mapOf<AxisAlignedBB, Int>()
    private val gemstoneBlockMap = Gemstone.entries.associateBy { it.metadata }.toMap()

    init {

        parameter(50) {
            matchKeyCategory()
            key = "radius"
            title = "Gemstone Scan Radius"

            settings {
                minValueInt = 10
                maxValueInt = 100
            }
        }

        listParameter(emptyList<Gemstone>()) {
            matchKeyCategory()
            key = "selectedGemstoneTypes"
            title = "Gemstone Types"

            autoFillEnum { it.readableName }
            settings { ordinal = 1 }
        }

        on<SpecialTickEvent>().filter { checkForRequirement() && scanTimer.update().check() }.subscribe {
            GlobalScope.launch {
                val current = BlockPos(mc.thePlayer)

                val curX = current.x
                val curY = current.y
                val curZ = current.z

                val from = BlockPos(curX - radius, max(curY - radius, 0), curZ - radius)
                val to = BlockPos(curX + radius, min(curY + radius, 255), curZ + radius)
                gemstoneBlocks = buildMap {
                    for (pos in BlockPos.getAllInBox(from, to)) {
                        val blockState = mc.theWorld.getBlockState(pos)
                        val block = blockState.block

                        if (block in Blocks.stained_glass_pane to Blocks.stained_glass) {
                            val gemstone = gemstoneBlockMap[block.getMetaFromState(blockState)] ?: continue
                            if (gemstone in selectedGemstoneTypes) {
                                put(pos.getAxisAlignedBB(), gemstone.color)
                            }
                        }
                    }
                }
            }
        }

        on<RenderWorldLastEvent>().filter { checkForRequirement() }.subscribe {
            for ((aabb, color) in gemstoneBlocks) {
                RenderUtils.drawBox(aabb, color, partialTicks)
            }
        }
    }
}