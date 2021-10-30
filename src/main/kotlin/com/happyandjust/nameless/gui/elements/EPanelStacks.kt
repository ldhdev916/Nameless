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

import com.happyandjust.nameless.devqol.mid
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import java.util.*

class EPanelStacks(rectangle: Rectangle) : EPanel(rectangle) {

    private val panelStack = LinkedList<EPanel>()
    private val goBackButton = EButton(Rectangle.ORIGIN, "Go Back", true).also {
        it.onClick = { pop() }
    }
    var onStateChange = {}

    fun push(panel: EPanel) {

        if (panelStack.isNotEmpty()) {
            removeChild(panelStack.peek())

            panel.rectangle = panel.rectangle.copy(top = rectangle.top + (rectangle.height) / 9)

            if (!childPanels.contains(goBackButton)) {

                val w = rectangle.width / 10
                val h = (rectangle.height) / 18

                goBackButton.rectangle = Rectangle.fromWidthHeight(
                    mid(rectangle.left, rectangle.right) - (w / 2),
                    mid(rectangle.top, panel.rectangle.top) - (h / 2),
                    w,
                    h
                )
                addChild(goBackButton)
            }

        }

        addChild(panel)
        panelStack.push(panel)

        onStateChange()
    }

    private fun pop() {
        if (panelStack.isNotEmpty()) {
            panelStack.pop().also { removeChild(it) }

            if (panelStack.size <= 1) {
                removeChild(goBackButton)
            }
        }

        if (panelStack.isNotEmpty()) {
            addChild(panelStack.peek())
        }

        onStateChange()
    }

    fun clear() {
        if (panelStack.isNotEmpty()) {
            removeChild(panelStack.peek())

            panelStack.clear()

            removeChild(goBackButton)
        }

        onStateChange()
    }

    fun peek(): EPanel? = panelStack.peek()

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {

    }
}