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

package com.happyandjust.nameless.mixinhooks

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.impl.qol.FeaturePerspective
import org.lwjgl.opengl.Display

object EntityRendererHook {

    var cameraYaw = 0F
        get() = if (FeaturePerspective.enabled) field else mc.thePlayer.rotationYaw
    var cameraPitch = 0F
        get() = if (FeaturePerspective.enabled) field else mc.thePlayer.rotationPitch
        set(value) {
            field = value.coerceIn(-90F, 90F)
        }

    fun overrideMouse(): Boolean {
        if (mc.inGameHasFocus && Display.isActive()) {
            if (!FeaturePerspective.enabled) return true

            mc.mouseHelper.mouseXYChange()

            val f1 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F
            val f2 = f1 * f1 * f1 * 8F
            val f3 = mc.mouseHelper.deltaX * f2
            val f4 = mc.mouseHelper.deltaY * f2

            cameraYaw += f3 * 0.15F
            cameraPitch -= f4 * 0.15F
        }

        return false
    }

}