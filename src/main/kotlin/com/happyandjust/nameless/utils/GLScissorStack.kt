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

package com.happyandjust.nameless.utils

import com.happyandjust.nameless.devqol.compress
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.util.*

/**
 * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 *
 * Modified
 *
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 *
 * @author Moulberry
 */
object GLScissorStack {

    private val boundStack = LinkedList<Bounds>()

    fun push(
        rectangle: Rectangle,
        scaleFactor: Int = ScaledResolution(mc).scaleFactor,
    ) {

        boundStack.push(
            if (boundStack.isEmpty()) Bounds(rectangle) else boundStack.peek()
                .createSubBound(rectangle)
        )

        if (boundStack.isNotEmpty()) {
            boundStack.peek().set(scaleFactor)
        }
        GL11.glEnable(GL11.GL_SCISSOR_TEST)
    }

    fun pop(scaleFactor: Int = ScaledResolution(mc).scaleFactor) {
        if (boundStack.isNotEmpty()) {
            boundStack.pop()
        }

        if (boundStack.isEmpty()) {
            GL11.glDisable(GL11.GL_SCISSOR_TEST)
        } else {
            boundStack.peek().set(scaleFactor)
        }
    }

    fun clear() {
        boundStack.clear()
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    private data class Bounds(val rectangle: Rectangle) {

        fun createSubBound(rectangle: Rectangle): Bounds {
            var left = rectangle.left.compress(this.rectangle.left)
            var top = rectangle.top.compress(this.rectangle.top)
            val right = rectangle.right.compress(max = this.rectangle.right)
            val bottom = rectangle.bottom.compress(max = this.rectangle.bottom)

            top = top.compress(max = bottom)
            left = left.compress(max = right)

            return Bounds(Rectangle(left, top, right, bottom))
        }

        fun set(scaleFactor: Int = ScaledResolution(mc).scaleFactor) {
            val height = mc.displayHeight

            GL11.glScissor(
                rectangle.left * scaleFactor,
                height - (rectangle.bottom * scaleFactor),
                (rectangle.right - rectangle.left).compress(0) * scaleFactor,
                (rectangle.bottom - rectangle.top).compress(0) * scaleFactor
            )
        }
    }
}