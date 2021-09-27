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
import com.happyandjust.nameless.devqol.disableDepth
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.devqol.pow
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EScrollBar(
    left: Int,
    right: Int,
    var minY: Int,
    var maxY: Int,
    var scrollBarColor: Int
) :
    EPanel(Rectangle(left, 0, right, 0)) {

    var totalHeight = 0


    fun adjustScrollBarPosition(scrollOffset: Int, maxScrollOffset: Int) {
        val scrollBarHeight = (maxY - minY).pow(2) / totalHeight

        val percent = -scrollOffset / maxScrollOffset.toDouble()

        val barTop = (minY + (maxY - minY - scrollBarHeight) * percent).toInt().compress(minY, maxY - scrollBarHeight)

        rectangle = rectangle.copy(top = barTop, bottom = barTop + scrollBarHeight)
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        if (totalHeight > (maxY - minY)) {
            disableDepth()

            rectangle.drawRect(scrollBarColor)
        }
    }
}