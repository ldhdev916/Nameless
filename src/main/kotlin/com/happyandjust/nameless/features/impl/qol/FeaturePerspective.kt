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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.FeatureStateListener
import com.happyandjust.nameless.features.listener.KeyInputListener
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.mixinhooks.EntityRendererHook

object FeaturePerspective : SimpleFeature(Category.QOL, "perspective", "Perspective", "Free Look"),
    FeatureStateListener,
    KeyInputListener {

    private var lastThirdPersonView = 0

    override fun onFeatureStateChangePre(e: FeatureStateChangeEvent.Pre) {

    }

    override fun onFeatureStateChangePost(e: FeatureStateChangeEvent.Post) {
        if (e.feature == this) {
            if (e.enabledAfter) {
                lastThirdPersonView = mc.gameSettings.thirdPersonView
                mc.gameSettings.thirdPersonView = 1
                EntityRendererHook.cameraYaw = mc.thePlayer.rotationYaw
                EntityRendererHook.cameraPitch = mc.thePlayer.rotationPitch
            } else {
                mc.gameSettings.thirdPersonView = lastThirdPersonView
            }
        }
    }

    override fun onKeyInput() {
        if (Nameless.INSTANCE.keyBindings[KeyBindingCategory.PERSPECTIVE]!!.isKeyDown) {
            invertEnableState()
        }
    }
}