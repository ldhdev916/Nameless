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

package com.happyandjust.nameless.textureoverlay

import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color

abstract class ERelocatablePanel(rectangle: Rectangle, var scale: Double) : EPanel(
    Rectangle(
        rectangle.left.compress(0),
        rectangle.top.compress(0),
        rectangle.right.compress(mc.displayWidth),
        rectangle.bottom.compress(mc.displayHeight)
    )
) {

    private var moving = false
    private var xOffset = 0
    private var yOffset = 0
    var sr: ScaledResolution = ScaledResolution(mc)
    protected var wheelSensitive = 2000.0


    private fun drawInformation() {
        matrix {
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
            translate(sr.scaledWidth / 2, sr.scaledHeight / 2, 0)

            mc.fontRendererObj.drawCenteredString(
                "Drag Element to Relocate, Scroll Mouse Wheel to Scale Up/Down",
                Color.lightGray.rgb,
                true
            )
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        }
    }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (isClickedInside) {
            moving = true

            xOffset = mouseX - rectangle.left
            yOffset = mouseY - rectangle.top
        }
    }

    final override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        if (!Mouse.isButtonDown(0)) {
            moving = false
        }

        if (moving) {
            val x = (mouseX - xOffset).compress(0, sr.scaledWidth)
            val y = (mouseY - yOffset).compress(0, sr.scaledHeight)

            rectangle = Rectangle.fromWidthHeight(x, y, rectangle.width, rectangle.height)
        }

        if (rectangle.isMouseInRectangle(mouseX, mouseY)) {
            while (Mouse.next()) {
                val wheel = Mouse.getEventDWheel()

                this.scale += (wheel / wheelSensitive)

                this.scale = this.scale.compress(0.005, 2.0)

                onUpdateScale(this.scale)

            }
        }

        matrix { drawElement() }

        rectangle.drawRect(0x531E1E1E)

        drawInformation()
    }

    abstract fun onUpdateScale(scale: Double)

    abstract fun drawElement()
}