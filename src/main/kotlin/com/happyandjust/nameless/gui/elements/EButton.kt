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
import com.happyandjust.nameless.devqol.drawOutlineBox
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

open class EButton(
    rectangle: Rectangle,
    displayString: String,
    private val hasOutline: Boolean = false
) :
    EPanel(rectangle) {

    var onClick: () -> Unit = {}
    var outlineColor = 0xFF009100.toInt()

    val displayLabel = ELabel(rectangle, Alignment.CENTER, Alignment.CENTER, displayString).also { addChild(it) }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (isClickedInside) {
            onClick()
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        val rect = if (hasOutline) rectangle.expand(-1, -1) else rectangle

        val hovered = rectangle.isMouseInRectangle(mouseX, mouseY)

        rect.drawRect((if (hovered) 0xFF464646 else 0xFF323232).toInt())
        if (hasOutline) {
            rectangle.drawOutlineBox(outlineColor)
        }
    }

    override fun onRectangleUpdate() {
        displayLabel.rectangle = rectangle
    }
}