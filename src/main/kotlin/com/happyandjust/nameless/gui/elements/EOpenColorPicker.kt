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

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class EOpenColorPicker(
    rectangle: Rectangle,
    private val currentColor: ChromaColor,
    private val storeColor: (ChromaColor) -> Unit,
    private val openMethod: (EColorPicker) -> Unit,
    private val colorPickerRectangle: Rectangle
) : EPanel(rectangle) {

    private val DEGREE_TO_RADIANS = PI / 180.0

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        val hovered = rectangle.isMouseInRectangle(mouseX, mouseY)

        val radius = rectangle.width / 2

        val centerX = mid(rectangle.left, rectangle.right)
        val centerY = mid(rectangle.top, rectangle.bottom)

        disableTexture2D()
        enableBlend()
        tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ZERO
        )
        shadeModel(GL11.GL_SMOOTH)
        disableCull()

        val wr = tessellator.worldRenderer

        wr.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_COLOR)

        repeat(360) {
            val x = radius * cos(it * DEGREE_TO_RADIANS)
            val y = radius * sin(it * DEGREE_TO_RADIANS)

            var color = Color.HSBtoRGB(it / 360F, 1F, 1F)

            if (hovered) {
                color = color and 0xFFFFFFB2.toInt()
            }

            wr.pos(x + centerX, y + centerY, 0.0).color(color).endVertex()
        }

        tessellator.draw()

        enableCull()
        shadeModel(GL11.GL_FLAT)
        disableBlend()
        enableTexture2D()
    }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (isClickedInside) {
            val colorPicker = EColorPicker(
                colorPickerRectangle,
                currentColor,
                storeColor
            )

            openMethod(colorPicker)
        }
    }
}