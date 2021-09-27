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

import com.happyandjust.nameless.devqol.matrix
import com.happyandjust.nameless.devqol.scissor
import java.util.concurrent.CopyOnWriteArrayList

abstract class EPanel(rectangle_: Rectangle) {

    private var lastClicked = 0L
    var rectangle = rectangle_
        set(value) {
            if (field != value) {
                field = value
                onRectangleUpdate()
            }
        }

    val childPanels = CopyOnWriteArrayList<EPanel>()

    fun addChild(childPanel: EPanel) {
        childPanels.add(childPanel)
    }

    fun removeChild(childPanel: EPanel) {
        childPanels.remove(childPanel)
    }

    fun mousePressed0(mouseX: Int, mouseY: Int) {
        for (childPanel in childPanels) {
            childPanel.mousePressed0(mouseX, mouseY)
        }

        var doubleClicked = false
        val isClickedInside = rectangle.isMouseInRectangle(mouseX, mouseY).also {
            if (it) {

                doubleClicked = System.currentTimeMillis() - lastClicked < 170

                lastClicked = System.currentTimeMillis()
            }
        }

        mousePressed(mouseX, mouseY, isClickedInside, doubleClicked)
    }

    fun mouseReleased0(mouseX: Int, mouseY: Int) {
        for (childPanel in childPanels) {
            childPanel.mouseReleased0(mouseX, mouseY)
        }

        mouseReleased(mouseX, mouseY)
    }

    fun keyTyped0(typedChar: Char, keyCode: Int) {
        for (childPanel in childPanels) {
            childPanel.keyTyped0(typedChar, keyCode)
        }

        keyTyped(typedChar, keyCode)
    }

    fun onUpdateScreen0() {
        for (childPanel in childPanels) {
            childPanel.onUpdateScreen0()
        }

        onUpdateScreen()
    }

    protected open fun onRectangleUpdate() {

    }

    protected open fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {}

    protected open fun mouseReleased(mouseX: Int, mouseY: Int) {}

    protected open fun keyTyped(typedChar: Char, keyCode: Int) {}

    protected open fun onUpdateScreen() {}

    fun drawPanel(mouseX: Int, mouseY: Int, scale: Int) {
        scissor(rectangle, scale) {
            matrix {
                draw(mouseX, mouseY, scale)
            }
        }

        drawChildPanels(mouseX, mouseY, scale)
    }

    protected open fun drawChildPanels(mouseX: Int, mouseY: Int, scale: Int) {
        for (childPanel in childPanels) {
            childPanel.drawPanel(mouseX, mouseY, scale)
        }
    }

    protected abstract fun draw(mouseX: Int, mouseY: Int, scale: Int)


}