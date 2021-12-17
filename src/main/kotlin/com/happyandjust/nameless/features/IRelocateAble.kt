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

package com.happyandjust.nameless.features

import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.gui.relocate.RelocateGui
import com.happyandjust.nameless.serialization.Converter
import gg.essential.elementa.UIComponent

interface IRelocateAble {

    /**
     * Make sure you use delegate to save to config
     */
    var overlayPoint: Overlay

    fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent

    fun getWheelSensitive() = 13

    fun getDisplayName(): String

    fun shouldDisplayInRelocateGui(): Boolean

    fun renderOverlay0(partialTicks: Float)
}

abstract class OverlayFeature(
    category: Category,
    key: String,
    title: String,
    desc: String = "",
    enabled_: Boolean = false
) : SimpleFeature(category, key, title, desc, enabled_), IRelocateAble {
    override fun getDisplayName() = title

    init {
        on<SpecialOverlayEvent>().filter { mc.currentScreen !is RelocateGui }.subscribe { renderOverlay0(partialTicks) }
    }
}

abstract class OverlayParameter<T>(
    ordinal: Int,
    category: String,
    key: String,
    title: String,
    desc: String,
    defaultValue: T,
    converter: Converter<T>
) : FeatureParameter<T>(ordinal, category, key, title, desc, defaultValue, converter), IRelocateAble {

    init {
        on<SpecialOverlayEvent>().filter { mc.currentScreen !is RelocateGui }.subscribe { renderOverlay0(partialTicks) }
    }

    override fun getDisplayName(): String {
        return title
    }

}