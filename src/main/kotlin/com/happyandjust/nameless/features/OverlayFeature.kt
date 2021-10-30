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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.serialization.converters.COverlay
import com.happyandjust.nameless.textureoverlay.ERelocatablePanel
import com.happyandjust.nameless.textureoverlay.ERelocateGui
import com.happyandjust.nameless.textureoverlay.Overlay

abstract class OverlayFeature(
    category: Category,
    key: String,
    title: String,
    desc: String = "",
    enabled_: Boolean = false
) : SimpleFeature(category, key, title, desc, enabled_), RenderOverlayListener {

    abstract val overlayPoint: ConfigValue<Overlay>

    fun getOverlayConfig(category: String, key: String, defaultOverlay: Overlay) = ConfigValue(
        category,
        key,
        defaultOverlay,
        COverlay
    )

    fun getRelocateGui() = ERelocateGui(getRelocatablePanel()) { overlayPoint.value = it }

    abstract fun getRelocatablePanel(): ERelocatablePanel
}