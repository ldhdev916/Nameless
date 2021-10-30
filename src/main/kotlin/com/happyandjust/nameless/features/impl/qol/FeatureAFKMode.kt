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

import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.FeatureStateListener
import com.happyandjust.nameless.features.listener.PlaySoundListener
import com.happyandjust.nameless.serialization.converters.CInt
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent

object FeatureAFKMode :
    SimpleFeature(
        Category.QOL,
        "afkmode",
        "AFK Mode",
        "Disable rendering blocks, entities, sounds, other things and limit fps"
    ),
    FeatureStateListener, PlaySoundListener {

    init {
        parameters["fps"] = FeatureParameter(
            0,
            "afkmode",
            "fps",
            "Limit FPS",
            "",
            15,
            CInt
        ).also {
            it.minValue = 5.0
            it.maxValue = 100.0
        }
    }

    override fun onFeatureStateChangePre(e: FeatureStateChangeEvent.Pre) {
    }

    override fun onFeatureStateChangePost(e: FeatureStateChangeEvent.Post) {
        if (e.enabledAfter && e.feature == this) {
            mc.thePlayer.closeScreen()
            mc.renderGlobal.loadRenderers()
        }
    }

    override fun onPlaySound(e: PlaySoundEvent) {
        if (!enabled) return
        e.result = null
    }

    override fun onPlaySoundAtEntity(e: PlaySoundAtEntityEvent) {
        if (!enabled) return
        e.isCanceled = true
    }
}