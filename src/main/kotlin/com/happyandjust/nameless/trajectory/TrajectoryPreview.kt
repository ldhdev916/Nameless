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

import com.happyandjust.nameless.dsl.mc
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.util.*

abstract class TrajectoryPreview {

    protected val entityPlayerSP: EntityPlayerSP = mc.thePlayer
    protected var posX = 0.0
    protected var posY = 0.0
    protected var posZ = 0.0
    protected var motionX = 0.0
    protected var motionY = 0.0
    protected var motionZ = 0.0
    protected var entityHit: Entity? = null
    private val random = Random()
    protected var gaussian = 0.0
    protected var bool = false
    protected open val shouldStop = { posY < 0 || !canMove() || checkEntity() }

    fun setRandomValue() {
        gaussian = random.nextGaussian()
        bool = random.nextBoolean()
    }


    fun init() {
        setInitialLocation()
    }

    protected abstract fun getAxisAlignedBB(): AxisAlignedBB

    protected abstract fun setInitialLocation()

    fun calculate(): TrajectoryCalculateResult {

        val traces = arrayListOf<Vec3>()
        var end: Vec3? = null

        run {
            while (true) {
                val addX = motionX / 120
                val addY = motionY / 120
                val addZ = motionZ / 120

                repeat(120) {
                    if (shouldStop()) return@run

                    end = Vec3(posX, posY, posZ)
                    traces.add(end!!)

                    posX += addX
                    posY += addY
                    posZ += addZ
                }

                calculateMotions()
            }
        }

        return TrajectoryCalculateResult(entityHit, end, traces)
    }

    protected abstract fun calculateMotions()

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
        val vectorA = Vec3(posX, posY, posZ)
        val addVector = vectorA.addVector(motionX, motionY, motionZ)
        val vectorB = mc.theWorld.rayTraceBlocks(vectorA, addVector)?.hitVec ?: addVector

        var entity: Entity? = null
        val aabb = getAxisAlignedBB()
        val list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
            entityPlayerSP,
            aabb.addCoord(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0)
        )
        var d0 = 0.0

        val f = 0.3
        list.filter { it.canBeCollidedWith() }.forEach {
            it.entityBoundingBox.expand(f, f, f).calculateIntercept(vectorA, vectorB)?.run {
                val d1 = vectorA.squareDistanceTo(hitVec)

                if (d1 < d0 || d0 == 0.0) {
                    entity = it
                    d0 = d1
                }
            }
        }

        this.entityHit = entity
        return entity != null

    }
}