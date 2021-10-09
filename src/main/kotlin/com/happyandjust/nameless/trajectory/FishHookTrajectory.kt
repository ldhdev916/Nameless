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

import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.PI

class FishHookTrajectory : TrajectoryPreview() {

    override fun getAxisAlignedBB() =
        AxisAlignedBB(posX - 0.125, posY, posZ - 0.125, posX + 0.125, posY + 0.25, posZ + 0.125)

    override fun setInitialLocation() {
        posX = entityPlayerSP.posX
        posY = entityPlayerSP.posY + entityPlayerSP.eyeHeight
        posZ = entityPlayerSP.posZ

        posX -= MathHelper.cos(entityPlayerSP.rotationYaw / 180F * PI.toFloat()) * 0.16F
        posY -= 0.10000000149011612
        posZ -= MathHelper.sin(entityPlayerSP.rotationYaw / 180F * PI.toFloat()) * 0.16F

        val f = 0.4F

        motionX =
            (-MathHelper.sin(entityPlayerSP.rotationYaw / 180.0f * PI.toFloat()) * MathHelper.cos(entityPlayerSP.rotationPitch / 180.0f * PI.toFloat()) * f).toDouble()
        motionZ =
            (MathHelper.cos(entityPlayerSP.rotationYaw / 180.0f * PI.toFloat()) * MathHelper.cos(entityPlayerSP.rotationPitch / 180.0f * PI.toFloat()) * f).toDouble()
        motionY = (-MathHelper.sin(entityPlayerSP.rotationPitch / 180.0f * PI.toFloat()) * f).toDouble()

        handleHookCasting(1.5F, 1F)
    }

    private fun handleHookCasting(velocity: Float, inaccuracy: Float) {
        val f = MathHelper.sqrt_double(motionX * motionX + motionY * motionY + motionZ * motionZ)

        var motionX = motionX / f
        var motionY = motionY / f
        var motionZ = motionZ / f

        motionX += gaussian * 0.007499999832361937 * inaccuracy
        motionY += gaussian * 0.007499999832361937 * inaccuracy
        motionZ += gaussian * 0.007499999832361937 * inaccuracy

        motionX *= velocity
        motionY *= velocity
        motionZ *= velocity

        this.motionX = motionX
        this.motionY = motionY
        this.motionZ = motionZ
    }

    override fun calculate(): TrajectoryCalculateResult {

        val list = arrayListOf<Vec3>()
        var end: Vec3? = null

        val num = 120

        label@ while (canMove()) {

            for (i in 0 until num) {
                if (posY < 0) break@label

                if (!canMove()) break@label

                if (checkEntity()) break@label

                // umm always stops if collide with water
                if (isInWater()) break@label

                list.add(Vec3(posX, posY, posZ))
                end = Vec3(posX, posY, posZ)

                posX += motionX / num
                posY += motionY / num
                posZ += motionZ / num
            }

            val f6 = 0.92F

            val d11 = -1.0

            motionY += 0.03999999910593033 * d11

            motionX *= f6
            motionY *= f6
            motionZ *= f6
        }

        return TrajectoryCalculateResult(entityHit, end, list)

    }
}