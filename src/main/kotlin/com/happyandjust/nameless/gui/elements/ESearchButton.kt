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

import com.happyandjust.nameless.devqol.color
import com.happyandjust.nameless.devqol.drawRect
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.gui.Gui
import net.minecraft.util.ChatAllowedCharacters
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard

class ESearchButton(rectangle: Rectangle, val textFieldWidth: Int, textScale: Double = 1.0) : EPanel(rectangle) {

    private val SEARCH_ICON = ResourceLocation("nameless", "search.png")
    var onTextFieldStateChange: (Boolean) -> Unit = {}
    var onTextFieldKeyTyped: (String) -> Unit = {}
    val textField: ETextField
    var textFieldShouldNotBeFocusedByKeyType: () -> Boolean = { false }

    init {
        addChild(object : ETextField(
            Rectangle.fromWidthHeight(
                rectangle.right - textFieldWidth,
                rectangle.top,
                textFieldWidth,
                rectangle.height
            ),
            textScale
        ) {

            init {
                maxStringWidth = 100
                isFocused = false
                visible = false
            }

            override fun keyTyped(typedChar: Char, keyCode: Int) {
                super.keyTyped(typedChar, keyCode)

                if (isFocused) {
                    onTextFieldKeyTyped(text)
                }
            }
        }.also { textField = it })
    }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (isClickedInside) {
            if (textField.visible) {
                disableTextField()
            } else {
                enableTextField()
            }
        }
    }

    fun disableTextField() {
        textField.isFocused = false.also(onTextFieldStateChange)
        textField.visible = false
        textField.text = ""

        rectangle = rectangle.offset(textField.rectangle.width, 0)
    }

    fun enableTextField() {
        textField.isFocused = true.also(onTextFieldStateChange)
        textField.visible = true

        rectangle = rectangle.offset(-textField.rectangle.width, 0)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE && textField.isFocused) {
            disableTextField()
        } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && !textField.isFocused && !textFieldShouldNotBeFocusedByKeyType()) {

            enableTextField()

            textField.writeText(typedChar.toString())

            onTextFieldKeyTyped(textField.text)
            // because textField, which is child panel of search button, and it's keyTyped method is invoked before searchButton's keyTyped is invoked
            // so TextField is not focused,visible, we need to write text manually at first

        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {

        val hovered = rectangle.isMouseInRectangle(mouseX, mouseY)

        if (hovered) {
            rectangle.drawRect(0xFF323232.toInt())
        }

        color(1f, 1f, 1f, 1f)
        mc.textureManager.bindTexture(SEARCH_ICON)

        Gui.drawModalRectWithCustomSizedTexture(
            rectangle.left,
            rectangle.top,
            0f,
            0f,
            rectangle.width,
            rectangle.height,
            rectangle.width.toFloat(),
            rectangle.height.toFloat()
        )
    }
}