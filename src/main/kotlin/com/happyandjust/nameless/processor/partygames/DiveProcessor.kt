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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.devqol.getBlockAtPos
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.awt.Color

object DiveProcessor : Processor(), ClientTickListener, WorldRenderListener {

    var boxColor = { -1 }

    private var axisAlignedBB: AxisAlignedBB? = null
    private var isCollide = false

    override fun tick() {

        val aabb = mc.thePlayer.entityBoundingBox

        axisAlignedBB = AxisAlignedBB(aabb.minX, 14.0, aabb.minZ, aabb.maxX, 15.0, aabb.maxZ).also {
            val w = mc.theWorld

            if (w.getBlockAtPos(BlockPos(it.minX, it.minY, it.minZ)) != Blocks.water) {
                isCollide = true
                return@also
            }
            if (w.getBlockAtPos(BlockPos(it.maxX, it.minY, it.minZ)) != Blocks.water) {
                isCollide = true
                return@also
            }
            if (w.getBlockAtPos(BlockPos(it.maxX, it.minY, it.maxZ)) != Blocks.water) {
                isCollide = true
                return@also
            }
            if (w.getBlockAtPos(BlockPos(it.minX, it.minY, it.maxZ)) != Blocks.water) {
                isCollide = true
                return@also
            }
            isCollide = false

        }
    }

    override fun renderWorld(partialTicks: Float) {
        axisAlignedBB?.let {
            RenderUtils.drawBox(it, if (isCollide) Color.red.rgb else boxColor(), partialTicks)
        }
    }
}