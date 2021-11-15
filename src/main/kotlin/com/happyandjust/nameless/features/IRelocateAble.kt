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
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.gui.relocate.RelocateGui
import gg.essential.elementa.UIComponent

interface IRelocateAble : RenderOverlayListener {

    val overlayPoint: ConfigValue<Overlay>

    fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent

    fun getWheelSensitive() = 7

    fun getDisplayName(): String

    fun shouldDisplayInRelocateGui(): Boolean

    fun renderOverlay0(partialTicks: Float)

    override fun renderOverlay(partialTicks: Float) {
        if (mc.currentScreen !is RelocateGui) renderOverlay0(partialTicks)
    }
}