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

package com.happyandjust.nameless.trajectory

import com.happyandjust.nameless.devqol.mc
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import java.util.*

abstract class TrajectoryPreview {

    companion object {
        val gaussian: Double
        val bool: Boolean

        init {
            val random = Random()
            gaussian = random.nextGaussian()
            bool = random.nextBoolean()
        }
    }

    protected val entityPlayerSP = mc.thePlayer
    protected var posX: Double = 0.0
    protected var posY: Double = 0.0
    protected var posZ: Double = 0.0
    protected var motionX: Double = 0.0
    protected var motionY: Double = 0.0
    protected var motionZ: Double = 0.0
    protected var entityHit: Entity? = null

    init {
        setInitialLocation()
    }

    protected abstract fun getAxisAlignedBB(): AxisAlignedBB

    protected abstract fun setInitialLocation()

    abstract fun calculate(): TrajectoryCalculateResult

    protected fun canMove() = when (mc.theWorld.getBlockState(BlockPos(posX, posY, posZ)).block) {
        Blocks.air, Blocks.water, Blocks.flowing_water -> true
        else -> false
    }


    protected fun isInWater(): Boolean {
        return when (mc.theWorld.getBlockState(BlockPos(posX, posY, posZ)).block) {
            Blocks.water, Blocks.flowing_water -> true
            else -> false
        }
    }

    protected fun checkEntity(): Boolean {
        var vec3 = Vec3(posX, posY, posZ)
        var vec31 = Vec3(posX + motionX, posY + motionY, posZ + motionZ)
        val movingObjectPosition: MovingObjectPosition? = mc.theWorld.rayTraceBlocks(vec3, vec31)
        vec3 = Vec3(posX, posY, posZ)
        vec31 = Vec3(posX + motionX, posY + motionY, posZ + motionZ)

        movingObjectPosition?.let { vec31 = Vec3(it.hitVec.xCoord, it.hitVec.yCoord, it.hitVec.zCoord) }

        var entity: Entity? = null
        val aabb = getAxisAlignedBB()
        val list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            entityPlayerSP,
            aabb.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0)
        )
        var d0 = 0.0

        for (entity1 in list) {
            if (entity1.canBeCollidedWith()) {
                val f = 0.3F
                val axisAlignedBB = entity1.entityBoundingBox.expand(f.toDouble(), f.toDouble(), f.toDouble())
                val movingObjectPosition1: MovingObjectPosition? = axisAlignedBB.calculateIntercept(vec3, vec31)

                movingObjectPosition1?.let {
                    val d1 = vec3.squareDistanceTo(it.hitVec)

                    if (d1 < d0 || d0 == 0.0) {
                        entity = entity1
                        d0 = d1
                    }
                }

            }
        }

        this.entityHit = entity
        return entity != null

    }
}
