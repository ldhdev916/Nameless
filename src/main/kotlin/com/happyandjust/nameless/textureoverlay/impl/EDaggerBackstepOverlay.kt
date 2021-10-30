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
import com.happyandjust.nameless.devqol.scale
import com.happyandjust.nameless.devqol.translate
import com.happyandjust.nameless.gui.Rectangle
import com.happyandjust.nameless.textureoverlay.ERelocatablePanel
import com.happyandjust.nameless.textureoverlay.Overlay
import java.awt.Color
import kotlin.math.roundToInt

class EDaggerBackstepOverlay(overlay: Overlay) : ERelocatablePanel(Rectangle.ORIGIN, overlay.scale) {

    private val text = "Backstep!"

    init {
        setRectangle(overlay.point.x, overlay.point.y)
    }

    override fun onUpdateScale(scale: Double) {
        setRectangle(rectangle.left, rectangle.top)
    }

    override fun drawElement() {
        translate(rectangle.left, rectangle.top, 0) {
            scale(scale, scale, 1.0)
            mc.fontRendererObj.drawString(text, 0, 0, Color.green.rgb)
        }
    }

    private fun setRectangle(left: Int, top: Int) {
        rectangle = Rectangle.fromWidthHeight(
            left,
            top,
            (mc.fontRendererObj.getStringWidth(text) * scale).roundToInt(),
            (mc.fontRendererObj.FONT_HEIGHT * scale).roundToInt()
        )
    }
}