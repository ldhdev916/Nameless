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

package com.happyandjust.nameless.gui

import kotlin.math.roundToInt

data class Rectangle(val left: Int, val top: Int, val right: Int, val bottom: Int) {


    constructor(left: Double, top: Double, right: Double, bottom: Double) : this(
        left.roundToInt(),
        top.roundToInt(),
        right.roundToInt(),
        bottom.roundToInt()
    )


    val width = right - left
    val height = bottom - top

    fun isMouseInRectangle(mouseX: Int, mouseY: Int) = mouseX in left..right && mouseY in top..bottom

    fun offset(x: Int, y: Int) = fromWidthHeight(left + x, top + y, width, height)

    fun expand(x: Int, y: Int) = Rectangle(left - x, top - y, right + x, bottom + y)

    fun validate() = width > 0 && height > 0

    operator fun times(value: Double) = Rectangle(left * value, top * value, right * value, bottom * value)

    operator fun div(value: Double) = Rectangle(left / value, top / value, right / value, bottom / value)

    companion object {

        val ORIGIN = Rectangle(0, 0, 0, 0)

        fun fromWidthHeight(left: Int, top: Int, width: Int, height: Int) =
            Rectangle(left, top, left + width, top + height)
    }
}
