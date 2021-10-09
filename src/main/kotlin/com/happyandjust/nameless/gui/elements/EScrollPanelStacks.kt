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

class EScrollPanelStacks(rectangle: Rectangle) : EPanel(rectangle) {

    private val scrollStack = LinkedList<EScrollPanel>()
    private val goBackButton = EButton(Rectangle.ORIGIN, "Go Back", true).also {
        it.onClick = { pop() }
    }
    var onStateChange = {}

    fun push(scrollPanel: EScrollPanel) {

        if (scrollStack.isNotEmpty()) {
            removeChild(scrollStack.peek())

            scrollPanel.rectangle = scrollPanel.rectangle.copy(top = rectangle.top + (rectangle.height) / 9)

            if (!childPanels.contains(goBackButton)) {

                val w = rectangle.width / 10
                val h = (rectangle.height) / 18

                goBackButton.rectangle = Rectangle.fromWidthHeight(
                    mid(rectangle.left, rectangle.right) - (w / 2),
                    mid(rectangle.top, scrollPanel.rectangle.top) - (h / 2),
                    w,
                    h
                )
                addChild(goBackButton)
            }

        }

        addChild(scrollPanel)
        scrollStack.push(scrollPanel)

        onStateChange()
    }

    private fun pop() {
        if (scrollStack.isNotEmpty()) {
            scrollStack.pop().also { removeChild(it) }

            if (scrollStack.size <= 1) {
                removeChild(goBackButton)
            }
        }

        if (scrollStack.isNotEmpty()) {
            addChild(scrollStack.peek())
        }

        onStateChange()
    }

    fun clear() {
        if (scrollStack.isNotEmpty()) {
            removeChild(scrollStack.peek())

            scrollStack.clear()

            removeChild(goBackButton)
        }

        onStateChange()
    }

    fun peek(): EScrollPanel? = scrollStack.peek()

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {

    }
}