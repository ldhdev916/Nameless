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

package com.happyandjust.nameless.textureoverlay.impl

import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.impl.FeatureTextureOverlay
import com.happyandjust.nameless.gui.Rectangle
import com.happyandjust.nameless.textureoverlay.ERelocatablePanel
import com.happyandjust.nameless.textureoverlay.Overlay
import net.minecraft.client.gui.Gui

class ETextureOverlay(overlay: Overlay, val overlayInfo: FeatureTextureOverlay.OverlayInfo) :
    ERelocatablePanel(
        Rectangle.ORIGIN,
        overlay.scale
    ) {

    init {

        wheelSensitive = 8000.0

        rectangle = Rectangle.fromWidthHeight(
            overlay.point.x,
            overlay.point.y,
            (overlayInfo.width * scale).toInt(),
            (overlayInfo.height * scale).toInt()
        )
    }

    override fun onUpdateScale(scale: Double) {
        rectangle = Rectangle.fromWidthHeight(
            rectangle.left,
            rectangle.top,
            (overlayInfo.width * scale).toInt(),
            (overlayInfo.height * scale).toInt()
        )
    }

    override fun drawElement() {
        mc.textureManager.bindTexture(overlayInfo.resourceLocation)

        Gui.drawModalRectWithCustomSizedTexture(
            rectangle.left,
            rectangle.top,
            0f,
            0f,
            rectangle.width,
            rectangle.height,
            rectangle.width.toFloat(),
            rectangle.height.toFloat()
        )
    }
}