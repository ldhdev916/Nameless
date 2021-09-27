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

package com.happyandjust.nameless

import com.happyandjust.nameless.devqol.toVec3
import net.minecraft.entity.Entity
import net.minecraft.util.Vec3
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.sqrt

data class Location(val vec3: Vec3) {
    var yaw = 0f
    var pitch = 0f

    constructor(source: Entity) : this(source.toVec3()) {
        yaw = source.rotationYaw
        pitch = source.rotationPitch
    }

    fun lookAt(location: Location) {
        setDirection((location - this).vec3)
    }

    operator fun minus(vec: Location) = Location(vec3.subtract(vec.vec3)).also {
        it.yaw = yaw - vec.yaw
        it.pitch = pitch - vec.pitch
    }

    fun setDirection(vector: Vec3) {
        val x = vector.xCoord
        val z = vector.zCoord
        val pitch: Float
        var yaw = yaw
        if (x == 0.0 && z == 0.0) {
            pitch = if (vector.yCoord == 0.0) {
                this.pitch
            } else {
                (if (vector.yCoord > 0.0) -90 else 90).toFloat()
            }
        } else {
            val theta = atan2(-x, z)

            yaw = Math.toDegrees((theta + Math.PI * 2) % (Math.PI * 2)).toFloat()

            val x2 = x * x
            val z2 = z * z
            val xz = sqrt(x2 + z2)

            pitch = Math.toDegrees(atan(-vector.yCoord / xz)).toFloat()
        }
        this.yaw = yaw
        this.pitch = pitch
    }
}
