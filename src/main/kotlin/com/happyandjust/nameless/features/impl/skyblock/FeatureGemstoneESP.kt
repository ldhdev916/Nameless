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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.contains
import com.happyandjust.nameless.dsl.getAxisAlignedBB
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.hypixel.skyblock.Gemstone
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent

object FeatureGemstoneESP : SimpleFeature(
    Category.SKYBLOCK,
    "gemstoneesp",
    "Gemstone ESP",
    "Render box on gemstones in SkyBlock Crystal Hollows"
) {

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty<String>(PropertyKey.ISLAND) == "crystal_hollows"

    private val scanTimer = TickTimer.withSecond(2)
    private val gemstoneBlocks = hashMapOf<AxisAlignedBB, Int>()
    private val gemstoneBlockMap = Gemstone.values().associateBy { it.metadata }.toMap()

    private var radius by FeatureParameter(
        0,
        "gemstoneesp",
        "radius",
        "Gemstone Scan Radius",
        "",
        50,
        CInt
    ).also {
        it.minValue = 10.0
        it.maxValue = 200.0
    }

    init {

        for (gemstone in Gemstone.values()) {

            val lowercase = gemstone.name.lowercase()

            parameters[lowercase] = FeatureParameter(
                gemstone.ordinal + 1,
                "gemstoneesp",
                lowercase,
                "Enable ${gemstone.readableName} ESP",
                "",
                false,
                CBoolean
            )
        }

        on<SpecialTickEvent>().filter { checkForRequirement() && scanTimer.update().check() }.subscribe {

            val current = BlockPos(mc.thePlayer)

            val curX = current.x
            val curY = current.y
            val curZ = current.z

            val from = BlockPos.MutableBlockPos(curX - radius, curY - radius, curZ - radius)
            val to = BlockPos.MutableBlockPos(curX + radius, curY + radius, curZ + radius)

            if (from.y < 0) from.set(from.x, 0, from.z)
            if (to.y > 255) to.set(to.x, 255, to.z)

            gemstoneBlocks.clear()
            for (pos in BlockPos.getAllInBox(from, to)) {
                val blockState = mc.theWorld.getBlockState(pos)
                val block = blockState.block

                if (block in Blocks.stained_glass_pane to Blocks.stained_glass) {
                    gemstoneBlocks[pos.getAxisAlignedBB()] =
                        (gemstoneBlockMap[block.getMetaFromState(blockState)]
                            ?.takeIf { getParameterValue(it.name.lowercase()) }
                            ?: continue).color
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