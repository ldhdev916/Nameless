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

package com.happyandjust.nameless.pathfinding

import com.happyandjust.nameless.devqol.getBlockAtPos
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.utils.Utils
import net.minecraft.block.BlockFence
import net.minecraft.block.BlockFenceGate
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.pathfinding.PathPoint
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.world.IBlockAccess
import net.minecraft.world.pathfinder.NodeProcessor
import java.util.*
import kotlin.concurrent.timerTask

class NodeProcessorPath(val canFly: Boolean) : NodeProcessor() {

    companion object {
        private val facings = EnumFacing.values()
    }

    private var shouldEnd = false

    override fun initProcessor(iblockaccessIn: IBlockAccess?, entityIn: Entity?) {
        super.initProcessor(iblockaccessIn, entityIn)
        shouldEnd = false
        Timer().schedule(
            timerTask {
                shouldEnd = true
            },
            800
        )
    }

    private fun jumpCheck(pos: BlockPos): Boolean {
        val state = mc.theWorld.getBlockState(pos)
        val block = state.block

        if (block is BlockFence) return false
        if (block !is BlockFenceGate) return true

        return state.getValue(BlockFenceGate.OPEN)
    }

    private fun isValid(pos: BlockPos): Boolean {
        val b = mc.theWorld.getBlockAtPos(pos)
        return if (!canFly && !jumpCheck(pos.down())) false else b != Blocks.web && b.isPassable(mc.theWorld, pos)
    }


    override fun getPathPointTo(entityIn: Entity): PathPoint = openPoint(
        MathHelper.floor_double(entityIn.posX),
        MathHelper.floor_double(entityIn.posY),
        MathHelper.floor_double(entityIn.posZ)
    )

    override fun getPathPointToCoords(entityIn: Entity?, x: Double, y: Double, z: Double): PathPoint = openPoint(
        MathHelper.floor_double(x),
        MathHelper.floor_double(y),
        MathHelper.floor_double(z)
    )

    override fun findPathOptions(
        pathOptions: Array<PathPoint>,
        entityIn: Entity,
        currentPoint: PathPoint,
        targetPoint: PathPoint,
        maxDistance: Float
    ): Int {

        if (shouldEnd) {
            return 0
        }

        var i = 0

        for (facing in facings) {
            val dir = facing.directionVec

            val newX = currentPoint.xCoord + dir.x
            val newY = currentPoint.yCoord + dir.y
            val newZ = currentPoint.zCoord + dir.z
            if (newY < 0) continue

            val current = BlockPos(newX, newY, newZ)
            val up = BlockPos(newX, newY + 1, newZ)

            if (!canFly) {
                if (entityIn.worldObj.getBlockAtPos(current) != Blocks.ladder && entityIn.worldObj.getBlockAtPos(up) != Blocks.ladder) {
                    if (newY > currentPoint.yCoord) {
                        val highestGround = Utils.getHighestGround(current, false)

                        if (newY - highestGround.y > 2) {
                            continue
                        }
                    }
                }
            }

            val pathPoint = openPoint(newX, newY, newZ)

            if (pathPoint.xCoord == targetPoint.xCoord && pathPoint.yCoord == targetPoint.yCoord && pathPoint.zCoord == targetPoint.zCoord) {
                pathOptions[i++] = pathPoint
                continue
            }

            if (pathPoint.distanceTo(targetPoint) <= 1.5) {
                if (isValid(current)) {
                    if (pathPoint.visited) continue
                    pathOptions[i++] = pathPoint

                    continue
                }
            }

            if (isValid(current) && isValid(up)) {
                if (pathPoint.visited) continue
                pathOptions[i++] = pathPoint
            }
        }

        return i
    }
}
