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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CInt
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent

object FeatureAFKMode :
    SimpleFeature(
        Category.QOL,
        "afkmode",
        "AFK Mode",
        "Disable rendering blocks, entities, sounds, other things and limit fps"
    ) {

    var fps by FeatureParameter(
        0,
        "afkmode",
        "fps",
        "Limit FPS",
        "",
        15,
        CInt
    ).apply {
        minValue = 5.0
        maxValue = 100.0
    }

    init {
        on<FeatureStateChangeEvent.Post>().filter { enabledAfter && feature == this@FeatureAFKMode }.subscribe {
            mc.thePlayer.closeScreen()
            mc.renderGlobal.loadRenderers()
        }

        on<PlaySoundEvent>().filter { enabled }.subscribe { result = null }

        on<PlaySoundAtEntityEvent>().filter { enabled }.subscribe { isCanceled = true }
    }
}