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
import com.happyandjust.nameless.devqol.scissor
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import org.lwjgl.input.Mouse

/**
 * @param elements EPanel's Rectangle need to be provided left, right, width, height, also don't update elements directly
 * use method [com.happyandjust.nameless.gui.elements.EScrollPanel.updateElements]
 */
open class EScrollPanel(
    rectangle: Rectangle,
    private val margin: Int = 0,
    private var elements: List<EPanel>
) : EPanel(rectangle) {

    var clickListener: (EPanel) -> Unit = {}
    private var scrollOffset = 0
    var selectedElement: EPanel? = null
    protected var mouseX = 0
    protected var mouseY = 0
    private var currentlyClicked = false
    private val border = 6
    private val scrollBar =
        EScrollBar(rectangle.right - 3, rectangle.right, rectangle.top + border, rectangle.bottom, 0xFF969696.toInt())

    init {
        for (element in elements) {
            addChild(element)
        }
        addChild(scrollBar)
    }

    override fun onRectangleUpdate() {
        scrollBar.minY = rectangle.top
        scrollBar.maxY = rectangle.bottom
    }

    fun updateElements(newElements: List<EPanel>) {
        for (element in elements) {
            removeChild(element)
        }

        for (element in newElements) {
            addChild(element)
        }

        elements = newElements
        scrollOffset = 0
    }

    protected open fun drawBackground() {

    }

    override fun drawChildPanels(mouseX: Int, mouseY: Int, scale: Int) {
        scissor(rectangle, scale) {
            super.drawChildPanels(mouseX, mouseY, scale)
        }
    }


    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        this.mouseX = mouseX
        this.mouseY = mouseY

        val hovered = rectangle.isMouseInRectangle(mouseX, mouseY)

        drawBackground()

        if (elements.isEmpty()) {
            scrollBar.totalHeight = 0
            return
        }

        scissor(rectangle) {
            var top = rectangle.top + border + scrollOffset

            var totalHeight = 0

            for (element in elements) {
                element.rectangle = Rectangle.fromWidthHeight(
                    element.rectangle.left,
                    top,
                    element.rectangle.width,
                    element.rectangle.height
                )

                top += (element.rectangle.height + margin).also { totalHeight += it }
            }

            val averageHeight = totalHeight / elements.size

            val clicked = Mouse.isButtonDown(0)

            val newlyClicked = !currentlyClicked && clicked

            currentlyClicked = clicked

            if (newlyClicked) {
                for (element in elements) {
                    if (element.rectangle.isMouseInRectangle(mouseX, mouseY)) {
                        clickListener(element)
                        selectedElement = element
                    }
                }
            }

            val maxScroll = (totalHeight - (rectangle.height - border)).compress(0)

            if (!currentlyClicked) {
                while (hovered && Mouse.next()) {
                    var scroll = Mouse.getEventDWheel()
                    if (scroll != 0) {
                        scroll = if (scroll > 0) 1 else -1

                        scrollOffset += (scroll * averageHeight / 2)

                        scrollOffset = scrollOffset.compress(-maxScroll, 0)
                    }
                }
            }

            scrollBar.totalHeight = totalHeight
            scrollBar.adjustScrollBarPosition(scrollOffset, maxScroll)
        }

    }
}