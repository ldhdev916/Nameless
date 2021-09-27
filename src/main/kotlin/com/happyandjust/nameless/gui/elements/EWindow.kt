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

import com.happyandjust.nameless.MOD_NAME
import com.happyandjust.nameless.VERSION
import com.happyandjust.nameless.core.Alignment
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EWindow(rectangle: Rectangle) : EPanel(rectangle) {

    val titleOffset = rectangle.height / 15

    init {
        addChild(
            ELabel(
                rectangle.copy(bottom = rectangle.top + titleOffset),
                Alignment.CENTER,
                Alignment.CENTER,
                "$MOD_NAME v$VERSION by HappyAndJust",
                1.3
            )
        )
    }


    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        rectangle.copy(top = rectangle.top + titleOffset).drawRect(0xFF282828.toInt())
        rectangle.copy(bottom = rectangle.top + titleOffset).drawRect(0xFF171717.toInt())
    }
}