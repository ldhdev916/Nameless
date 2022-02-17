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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.events.KeyPressEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.mixinhooks.EntityRendererHook

object Perspective : SimpleFeature("perspective", "Perspective", "Free Look") {

    @JvmStatic
    val enabledJVM
        get() = enabled
    private var lastThirdPersonView = 0

    init {
        on<FeatureStateChangeEvent.Post>().filter { feature == this@Perspective }.subscribe {
            if (enabledAfter) {
                lastThirdPersonView = mc.gameSettings.thirdPersonView
                mc.gameSettings.thirdPersonView = 1
                EntityRendererHook.cameraYaw = mc.thePlayer.rotationYaw
                EntityRendererHook.cameraPitch = mc.thePlayer.rotationPitch
            } else {
                mc.gameSettings.thirdPersonView = lastThirdPersonView
            }
        }

        on<KeyPressEvent>().filter { isNew && !inGui && keyBindingCategory == KeyBindingCategory.PERSPECTIVE }
            .subscribe {
                enabled = !enabled
            }
    }
}