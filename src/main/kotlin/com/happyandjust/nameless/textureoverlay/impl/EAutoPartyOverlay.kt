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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.Rectangle
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.textureoverlay.ERelocatablePanel
import com.happyandjust.nameless.textureoverlay.Overlay
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max
import kotlin.math.roundToInt

class EAutoPartyOverlay(overlay: Overlay) : ERelocatablePanel(Rectangle.ORIGIN, overlay.scale) {

    val keyBindings = Nameless.INSTANCE.keyBindings

    val text = """
        §6Party Request From §aSomeone
        §6Press [§a${Keyboard.getKeyName(keyBindings[KeyBindingCategory.ACCEPT_PARTY]!!.keyCode)}§6] to Accept [§c${
        Keyboard.getKeyName(
            keyBindings[KeyBindingCategory.DENY_PARTY]!!.keyCode
        )
    }§6] to Deny
    """.trimIndent()

    init {
        setRectangle(overlay.point.x, overlay.point.y)
    }

    private fun setRectangle(left: Int, top: Int) {
        val split = text.split("\n")

        val s1 = split[0]
        val s2 = split[1]

        val fontRenderer = mc.fontRendererObj

        val s1Width = fontRenderer.getStringWidth(s1)
        val s2Width = fontRenderer.getStringWidth(s2)

        val maxWidth = max(s1Width, s2Width)

        rectangle = Rectangle.fromWidthHeight(
            left,
            top,
            (maxWidth * scale).roundToInt(),
            (fontRenderer.FONT_HEIGHT * 4 * scale).roundToInt()
        )
    }

    override fun onUpdateScale(scale: Double) {
        setRectangle(rectangle.left, rectangle.top)
    }

    override fun drawElement() {
        rectangle.drawRect(0x28FFFFFF)

        translate(mid(rectangle.left, rectangle.right), rectangle.top, 0) {
            scale(scale, scale, 1.0)

            val fontRenderer = mc.fontRendererObj

            val split = text.split("\n")

            val s1 = split[0]
            val s2 = split[1]

            fontRenderer.drawStringWithShadow(
                s1,
                -(fontRenderer.getStringWidth(s1) / 2F),
                fontRenderer.FONT_HEIGHT.toFloat(),
                Color.white.rgb
            )

            fontRenderer.drawStringWithShadow(
                s2,
                -(fontRenderer.getStringWidth(s2) / 2F),
                (fontRenderer.FONT_HEIGHT * 2).toFloat(),
                Color.white.rgb
            )

        }
    }
}