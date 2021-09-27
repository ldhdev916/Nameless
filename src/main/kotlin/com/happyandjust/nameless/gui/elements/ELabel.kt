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

package com.happyandjust.nameless.gui.elements

import com.happyandjust.nameless.core.Alignment
import com.happyandjust.nameless.devqol.drawString
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.scale
import com.happyandjust.nameless.devqol.translate
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import java.awt.Color

class ELabel(
    rectangle: Rectangle,
    var xAlignment: Alignment,
    var yAlignment: Alignment,
    var text: String,
    var textScale: Double = 1.0,
    var textColor: Int = Color.white.rgb
) : EPanel(rectangle) {

    private val fontRenderer = mc.fontRendererObj


    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        translate(rectangle.left, rectangle.top, 0) {
            val texts = text.split("\n")
            var offsetY = 0

            for (text in texts) {
                val x = when (xAlignment) {
                    Alignment.LEFT -> {
                        0
                    }
                    Alignment.RIGHT -> {
                        translate(rectangle.width, 0, 0)
                        -fontRenderer.getStringWidth(text)
                    }
                    Alignment.CENTER -> {
                        translate(rectangle.width / 2, 0, 0)
                        -(fontRenderer.getStringWidth(text) / 2)
                    }
                    else -> throw IllegalArgumentException(xAlignment.name)
                }

                when (yAlignment) {
                    Alignment.TOP -> {
                        scale(textScale, textScale, 1.0)
                        fontRenderer.drawString(text, x, offsetY, textColor, true)
                    }
                    Alignment.CENTER -> {
                        translate(0, rectangle.height / 2, 0)
                        scale(textScale, textScale, 1.0)
                        fontRenderer.drawString(text, x, offsetY - (fontRenderer.FONT_HEIGHT / 2), textColor, true)
                    }
                    Alignment.BOTTOM -> {
                        translate(0, rectangle.height, 0)
                        scale(textScale, textScale, 1.0)
                        fontRenderer.drawString(text, x, offsetY - fontRenderer.FONT_HEIGHT, textColor, true)
                    }
                    else -> throw IllegalArgumentException(yAlignment.name)
                }

                offsetY += fontRenderer.FONT_HEIGHT
            }
        }
    }

}