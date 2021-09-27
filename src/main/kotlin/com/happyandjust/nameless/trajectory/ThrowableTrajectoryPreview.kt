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

class ThrowableTrajectoryPreview : TrajectoryPreview() {
    override fun getAxisAlignedBB(): AxisAlignedBB =
        AxisAlignedBB(posX - 0.125, posY, posZ - 0.125, posX + 0.125, posY + 0.25, posZ + 0.125)

    override fun setInitialLocation() {
        posX = entityPlayerSP.posX
        posY = entityPlayerSP.posY + entityPlayerSP.getEyeHeight().toDouble()
        posZ = entityPlayerSP.posZ
        posX -= (MathHelper.cos(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()
        posY -= 0.10000000149011612
        posZ -= (MathHelper.sin(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f).toDouble()
        val f = 0.4f
        motionX =
            (-MathHelper.sin(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(entityPlayerSP.rotationPitch / 180.0f * Math.PI.toFloat()) * f).toDouble()
        motionZ =
            (MathHelper.cos(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(entityPlayerSP.rotationPitch / 180.0f * Math.PI.toFloat()) * f).toDouble()
        motionY = (-MathHelper.sin(entityPlayerSP.rotationPitch / 180.0f * Math.PI.toFloat()) * f).toDouble()
        setThrowableHeading(motionX, motionY, motionZ, 1.5f, 1.0f)

    }

    fun setThrowableHeading(x: Double, y: Double, z: Double, velocity: Float, inaccuracy: Float) {
        var x = x
        var y = y
        var z = z
        val f = MathHelper.sqrt_double(x * x + y * y + z * z)
        x /= f.toDouble()
        y /= f.toDouble()
        z /= f.toDouble()
        x += gaussian * 0.007499999832361937 * inaccuracy.toDouble()
        y += gaussian * 0.007499999832361937 * inaccuracy.toDouble()
        z += gaussian * 0.007499999832361937 * inaccuracy.toDouble()
        x *= velocity.toDouble()
        y *= velocity.toDouble()
        z *= velocity.toDouble()
        motionX = x
        motionY = y
        motionZ = z
    }

    override fun calculate(): TrajectoryCalculateResult {
        val list = arrayListOf<Vec3>()
        var f4 = 0.99f
        val f6 = 0.03f

        val num = 120
        var end: Vec3? = null

        label@ while (canMove()) {
            for (i in 0 until num) {
                if (posY < 0) break@label
                if (!canMove()) {
                    break@label
                }
                if (checkEntity()) {
                    break@label
                }
                list.add(Vec3(posX, posY, posZ))
                end = Vec3(posX, posY, posZ)
                posX += motionX / num
                posY += motionY / num
                posZ += motionZ / num
            }
            if (isInWater()) {
                f4 = 0.8f
            }
            motionX *= f4.toDouble()
            motionY *= f4.toDouble()
            motionZ *= f4.toDouble()
            motionY -= f6.toDouble()
        }
        return TrajectoryCalculateResult(entityHit, end, list)

    }


}
