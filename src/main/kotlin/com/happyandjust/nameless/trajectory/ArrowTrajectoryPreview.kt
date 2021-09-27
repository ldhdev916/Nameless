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

class ArrowTrajectoryPreview : TrajectoryPreview() {
    override fun getAxisAlignedBB(): AxisAlignedBB =
        AxisAlignedBB(posX - 0.25, posY, posZ - 0.25, posX + 0.25, posY + 0.5, posZ + 0.25)

    override fun setInitialLocation() {
        posX = entityPlayerSP.posX - MathHelper.cos(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f
        posY = entityPlayerSP.posY + entityPlayerSP.getEyeHeight().toDouble() - 0.10000000149011612
        posZ = entityPlayerSP.posZ - MathHelper.sin(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16f
        motionX =
            (-MathHelper.sin(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(entityPlayerSP.rotationPitch / 180.0f * Math.PI.toFloat())).toDouble()
        motionZ =
            (MathHelper.cos(entityPlayerSP.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(entityPlayerSP.rotationPitch / 180.0f * Math.PI.toFloat())).toDouble()
        motionY = -MathHelper.sin(entityPlayerSP.rotationPitch / 180.0f * Math.PI.toFloat()).toDouble()
        var x = motionX
        var y = motionY
        var z = motionZ
        val f = MathHelper.sqrt_double(x * x + y * y + z * z)
        val i = entityPlayerSP.itemInUseDuration
        var v = i / 20.0f
        v = (v * v + v * 2.0f) / 3.0f
        if (v > 1.0f) {
            v = 1.0f
        }
        val velocity = v * 2.0f * 1.5f
        val inaccuracy = 1.0f
        x /= f.toDouble()
        y /= f.toDouble()
        z /= f.toDouble()
        x += gaussian * (if (bool) -1 else 1).toDouble() * 0.007499999832361937 * inaccuracy.toDouble()
        y += gaussian * (if (bool) -1 else 1).toDouble() * 0.007499999832361937 * inaccuracy.toDouble()
        z += gaussian * (if (bool) -1 else 1).toDouble() * 0.007499999832361937 * inaccuracy.toDouble()
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
        val f6 = 0.05f
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
                f4 = 0.6f
            }
            motionX *= f4.toDouble()
            motionY *= f4.toDouble()
            motionZ *= f4.toDouble()
            motionY -= f6.toDouble()
        }
        return TrajectoryCalculateResult(entityHit, end, list)
    }


}