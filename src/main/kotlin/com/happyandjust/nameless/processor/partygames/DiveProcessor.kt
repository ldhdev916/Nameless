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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.dive_color
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object DiveProcessor : Processor() {

    private var axisAlignedBB: AxisAlignedBB? = null
    private var isCollide = false
    override val filter = PartyGamesHelper.getFilter(this)

    init {
        request<SpecialTickEvent>().subscribe {
            val aabb = mc.thePlayer.entityBoundingBox

            axisAlignedBB = AxisAlignedBB(aabb.minX, 14.0, aabb.minZ, aabb.maxX, 15.0, aabb.maxZ).also {
                val w = mc.theWorld
                isCollide = sequence {
                    for (x in arrayOf(it.minX, it.maxX)) {
                        for (z in arrayOf(it.minZ, it.maxZ)) {
                            yield(BlockPos(x, it.minY, z))
                        }
                    }
                }.any { pos -> w.getBlockAtPos(pos) != Blocks.water }
            }
        }

        request<RenderWorldLastEvent>().subscribe {
            axisAlignedBB?.let {
                RenderUtils.drawBox(
                    it,
                    if (isCollide) Color.red.rgb else PartyGamesHelper.dive_color.rgb,
                    partialTicks
                )
            }
        }
    }
}