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
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

class EInCategory(parent: EScrollPanel, name: String) :
    EPanel(
        Rectangle(
            parent.rectangle.left + 4,
            0,
            parent.rectangle.right - 4,
            (mc.fontRendererObj.FONT_HEIGHT * 0.85).roundToInt()
        )
    ) {

    private val label = ELabel(rectangle, Alignment.CENTER, Alignment.CENTER, name, 0.85).also { addChild(it) }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        disableTexture2D()
        enableBlend()
        tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        color(Color.lightGray.rgb)

        val wr = tessellator.worldRenderer

        val textWidth = mc.fontRendererObj.getStringWidth(label.text) * 0.85
        val centerX = mid(rectangle.left, rectangle.right)
        val centerY = mid(rectangle.top, rectangle.bottom)

        val border = 6

        wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

        wr.pos(rectangle.left, centerY, 0).endVertex()
        wr.pos(centerX - (textWidth / 2) - border, centerY.toDouble(), 0.0).endVertex()

        wr.pos(centerX + (textWidth / 2) + border, centerY.toDouble(), 0.0).endVertex()
        wr.pos(rectangle.right, centerY, 0).endVertex()

        tessellator.draw()

        disableBlend()
        enableTexture2D()
    }

    override fun onRectangleUpdate() {
        label.rectangle = rectangle
    }
}