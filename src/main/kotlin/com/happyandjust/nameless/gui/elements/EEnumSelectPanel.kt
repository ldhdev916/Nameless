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
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EEnumSelectPanel<E : Enum<*>>(
    rectangle: Rectangle,
    private var currentEnumType: E,
    private val allEnums: List<E>
) : EPanel(rectangle) {

    var onCurrentEnumChange: (E) -> Unit = {}

    private val displayLabelPanel = ELabel(rectangle, Alignment.CENTER, Alignment.CENTER, currentEnumType.name).also {
        addChild(it)
    }
    private val previousButton =
        EButton(Rectangle.fromWidthHeight(0, 0, rectangle.width / 5, rectangle.height), "<").also {
            addChild(it)

            it.onClick = {
                var newIndex = allEnums.indexOf(currentEnumType) - 1

                if (newIndex < 0) newIndex = allEnums.size - 1

                val newEnum = allEnums[newIndex]

                displayLabelPanel.text = newEnum.name

                currentEnumType = newEnum
                onCurrentEnumChange(newEnum)
            }
        }
    private val nextButton = EButton(Rectangle.fromWidthHeight(0, 0, rectangle.width / 5, rectangle.height), ">").also {
        addChild(it)

        it.onClick = {
            var newIndex = allEnums.indexOf(currentEnumType) + 1

            if (newIndex >= allEnums.size) newIndex = 0

            val newEnum = allEnums[newIndex]

            displayLabelPanel.text = newEnum.name

            currentEnumType = newEnum
            onCurrentEnumChange(newEnum)
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        rectangle.drawRect(0xFF323232.toInt())
    }

    override fun onRectangleUpdate() {
        displayLabelPanel.rectangle = rectangle

        val buttonWidth = rectangle.width / 5

        previousButton.rectangle = Rectangle.fromWidthHeight(
            rectangle.left - buttonWidth,
            rectangle.top,
            buttonWidth,
            rectangle.height
        )

        nextButton.rectangle = Rectangle.fromWidthHeight(
            rectangle.right,
            rectangle.top,
            buttonWidth,
            rectangle.height
        )
    }
}