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

import com.happyandjust.nameless.devqol.compress
import com.happyandjust.nameless.devqol.drawOutlineBox
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.devqol.mid
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EToggleButton(rectangle: Rectangle, var enabled: Boolean, private val onClick: (Boolean) -> Unit) :
    EPanel(rectangle) {

    private val ANIMATION_TIME = 120
    private var lastClickedTime = -1L

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (isClickedInside) {
            enabled = !enabled

            onClick(enabled)
            lastClickedTime = System.currentTimeMillis()
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {

        val hovered = rectangle.isMouseInRectangle(mouseX, mouseY)

        val outlineColor: Int
        val boxColor: Int

        if (enabled) {
            outlineColor = 0xFF01A552.toInt()
            boxColor = if (hovered) 0xFF007339.toInt() else outlineColor
        } else {
            outlineColor = 0xFF979797.toInt()
            boxColor = if (hovered) 0xFF696969.toInt() else outlineColor
        }

        var box = getBoxRectangle(enabled)

        if (lastClickedTime != -1L) {
            box = getBoxRectangle(!enabled)

            val timeSinceOpen = (System.currentTimeMillis() - lastClickedTime).toInt().compress(max = ANIMATION_TIME)

            val offset = ((rectangle.width / 2) * (timeSinceOpen.toDouble() / ANIMATION_TIME)).toInt()

            box = box.offset(if (enabled) offset else -offset, 0)
        }
        box.drawRect(boxColor)

        rectangle.drawOutlineBox(outlineColor)

    }

    private fun getBoxRectangle(enabled: Boolean): Rectangle {
        val mid = mid(rectangle.left, rectangle.right)

        return if (enabled) {
            rectangle.copy(left = mid)
        } else {
            rectangle.copy(right = mid)
        }
    }


}