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

package com.happyandjust.nameless.pathfinding

import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import net.minecraft.block.Block
import net.minecraft.block.BlockFence
import net.minecraft.block.BlockFenceGate
import net.minecraft.block.BlockLadder
import net.minecraft.block.material.Material
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.pathfinding.PathPoint
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3i
import net.minecraft.world.IBlockAccess
import net.minecraft.world.pathfinder.NodeProcessor
import kotlin.math.abs

class NodeProcessorPath(
    private val canFly: Boolean,
    private val timeout: Long,
    private val cache: Boolean,
    private val additionalValidCheck: (BlockPos) -> Boolean
) : NodeProcessor() {

    companion object {
        private val directionVectors = buildSet {
            for (x in -1..1) {
                for (y in -1..1) {
                    for (z in -1..1) {
                        if (x == 0 && y == 0 && z == 0) continue
                        val diagonal = abs(x) + abs(y) + abs(z) != 1

                        add(Vec3i(x, y, z) to diagonal)
                    }
                }
            }
        }
    }

    private var endTimeMillis = 0L
    private val passableMap = hashMapOf<BlockPos, Boolean>()
    private val blockMap = hashMapOf<BlockPos, Block>()

    private fun BlockPos.isPassable() = if (cache) {
        passableMap.getOrPut(this) { noCachePassable() }
    } else noCachePassable()

    private fun BlockPos.noCachePassable() =
        mc.theWorld.isBlockLoaded(this, false) && getBlockAtPos(this).isPassable(mc.theWorld, this)

    private fun getBlockAtPos(pos: BlockPos) =
        if (cache) blockMap.getOrPut(pos) { mc.theWorld.getBlockAtPos(pos) } else mc.theWorld.getBlockAtPos(pos)

    override fun initProcessor(iblockaccessIn: IBlockAccess?, entityIn: Entity?) {
        super.initProcessor(iblockaccessIn, entityIn)
        endTimeMillis = System.currentTimeMillis() + timeout
    }

    private fun jumpCheck(pos: BlockPos): Boolean {
        val state = mc.theWorld.getBlockState(pos)
        val block = state.block

        if (block is BlockFence) return false
        if (block !is BlockFenceGate) return true

        return state.getValue(BlockFenceGate.OPEN)
    }

    private fun isValid(pos: BlockPos) =
        if (!canFly && !jumpCheck(pos.down())) false else pos.isPassable() && additionalValidCheck(pos)


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

        if (System.currentTimeMillis() >= endTimeMillis) {
            return 0
        }

        var i = 0

        for ((dir, isDiagonal) in directionVectors) {

            val newX = currentPoint.xCoord + dir.x
            val newY = currentPoint.yCoord + dir.y
            val newZ = currentPoint.zCoord + dir.z
            if (newY !in 0..255) continue

            val current = BlockPos(newX, newY, newZ)
            val up = BlockPos(newX, newY + 1, newZ)

            if (isUnableToMove(dir, current, isDiagonal)) continue

            val pathPoint = openPoint(newX, newY, newZ)

            if (pathPoint == targetPoint) {
                pathOptions[i++] = pathPoint
                continue
            }

            if (pathPoint.distanceTo(targetPoint) <= 1.5 && isValid(current)) {
                if (pathPoint.visited) continue
                pathOptions[i++] = pathPoint
                continue
            }

            if (isValid(current) && isValid(up)) {
                if (pathPoint.visited) continue
                pathOptions[i++] = pathPoint
            }
        }

        return i
    }

    private fun isUnableToMove(dir: Vec3i, current: BlockPos, isDiagonal: Boolean): Boolean {
        val (curX, curY, curZ) = Triple(current.x, current.y, current.z)
        val (prevX, prevY, prevZ) = Triple(curX - dir.x, curY - dir.y, curZ - dir.z)

        if (!canFly && dir.y > 0 && getBlockAtPos(current) != Blocks.ladder) {
            val check: BlockPos.() -> Boolean = {
                curY - getHighestGround(this).y > 2
            }
            if (BlockPos(prevX, prevY, prevZ).check()) return true
            if (BlockPos(curX + dir.x, curY + dir.y, curZ + dir.z).check()) return true
        }

        if (isDiagonal) {
            val subX = current.add(-dir.x, 0, 0)
            val subZ = current.add(0, 0, -dir.z)

            if ((!isValid(subX) || !isValid(subX.up())) && (!isValid(subZ) || !isValid(subZ.up()))) return true
        }
        return false
    }

    private fun getHighestGround(posParam: BlockPos): BlockPos {
        var pos = posParam
        while (getBlockAtPos(pos).canPassThrough(pos)) {
            pos = pos.down()
            if (pos.y <= 0) return pos
        }

        return pos
    }

    private fun Block.canPassThrough(pos: BlockPos) =
        isPassable(mc.theWorld, pos) && material != Material.water && this !is BlockLadder
}