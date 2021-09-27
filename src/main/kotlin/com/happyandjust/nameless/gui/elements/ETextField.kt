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
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.roundToInt

open class ETextField(rectangle: Rectangle, private val textScale: Double = 1.0) : EPanel(rectangle) {

    private val fontRenderer = mc.fontRendererObj
    var cursorPosition = 0
        get() = field.compress(0, text.length)
        set(value) {
            field = value.compress(0, text.length)
        }

    var text = ""
        set(value) {
            if (validator(value)) {
                field = if (value.length > maxStringWidth) value.substring(0, maxStringWidth) else value
            }
        }

    private val scrollOffset: Int
        get() = (getScaledStringWidth(text.substring(0, cursorPosition)) - rectangle.width).compress(0)

    var maxStringWidth = 32
        set(value) {
            field = value

            if (value < text.length) {
                text = text.substring(0, value)
            }
        }

    var validator: (String) -> Boolean = { true }

    private val selectedText: String
        get() {
            return if (selectedStart != -1 && selectedEnd != -1) {
                text.substring(selectedStart..selectedEnd)
            } else {
                ""
            }
        }

    private var selectedStart = -1
        set(value) {
            field = value.compress(-1, text.length - 1)
        }
    private var selectedEnd = -1
        set(value) {
            field = value.compress(-1, text.length - 1)
        }

    private var cursorCounter = 0

    open var visible = true

    open var isFocused = false
        set(value) {
            if (value && !field) {
                cursorCounter = 0
            }
            field = value
        }

    private val textLabel =
        ELabel(Rectangle.ORIGIN, Alignment.LEFT, Alignment.CENTER, "", textScale).also { addChild(it) }

    fun isEmpty() = text.isEmpty()
    fun isNotEmpty() = text.isNotEmpty()
    fun isBlank() = text.isBlank()
    fun isNotBlank() = text.isNotBlank()

    override fun onUpdateScreen() {
        cursorCounter++
    }

    fun writeText(s: String) {

        var s = ChatAllowedCharacters.filterAllowedCharacters(s)

        if (selectedText.isNotEmpty()) {
            if (cursorPosition in selectedStart..selectedEnd) {
                cursorPosition = cursorPosition.compress(selectedStart)
            } else if (cursorPosition > selectedEnd) {
                cursorPosition -= selectedText.length
            }

            text = text.removeRange(selectedStart..selectedEnd)

            resetSelectedText()
        }

        val overLength = (s.length + text.length) - maxStringWidth

        if (overLength > 0) {
            s = s.substring(0, s.length - overLength)
        }

        if (s.isEmpty()) return

        val textBeforeCursor = text.substring(0, cursorPosition)
        val textAfterCursor = text.substring(cursorPosition)

        text = "$textBeforeCursor$s$textAfterCursor"

        cursorPosition += s.length

    }

    override fun mousePressed(mouseX: Int, mouseY: Int, isClickedInside: Boolean, doubleClicked: Boolean) {
        if (!visible) return
        isFocused = isClickedInside

        if (isFocused && Mouse.isButtonDown(0)) {
            val diffX = mouseX - rectangle.left

            cursorPosition = fontRenderer.trimStringToWidth(text, diffX + scrollOffset).length

            selectedStart = -1
            selectedEnd = -1
        }
    }

    private fun resetSelectedText() {
        selectedStart = -1
        selectedEnd = -1
    }

    private fun deleteFromCursor() {
        if (text.isNotEmpty() && cursorPosition > 0) {

            val i = cursorPosition - 1

            text = text.removeRange(i..i)
            cursorPosition = i
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!isFocused) return
        if (!visible) return
        if (GuiScreen.isKeyComboCtrlA(keyCode)) {
            selectedStart = 0
            selectedEnd = text.length - 1

            cursorPosition = text.length

        } else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
            selectedText.takeIf { it.isNotEmpty() }?.copyToClipboard()
        } else if (GuiScreen.isKeyComboCtrlV(keyCode)) {
            writeText(GuiScreen.getClipboardString())
        } else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
            selectedText.copyToClipboard()
            writeText("")
        } else {
            when (keyCode) {
                Keyboard.KEY_BACK -> {
                    if (selectedText.isNotEmpty()) {
                        writeText("")
                    } else {
                        deleteFromCursor()
                    }
                }
                Keyboard.KEY_LEFT -> {
                    if (selectedText.isEmpty()) {
                        cursorPosition--
                    } else {
                        if (cursorPosition <= selectedStart) {
                            cursorPosition--
                        } else {
                            cursorPosition = selectedStart
                        }
                    }
                    resetSelectedText()
                }
                Keyboard.KEY_RIGHT -> {
                    if (selectedText.isEmpty()) {
                        cursorPosition++
                    } else {
                        if (cursorPosition >= selectedEnd) {
                            cursorPosition++
                        } else {
                            cursorPosition = selectedEnd
                        }
                    }
                    resetSelectedText()
                }
                Keyboard.KEY_SPACE -> {
                    writeText(" ")
                }
                else -> {
                    if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                        writeText(typedChar.toString())
                    }
                }
            }
        }
    }

    override fun drawChildPanels(mouseX: Int, mouseY: Int, scale: Int) {
        for (childPanel in childPanels) {
            if (childPanel == textLabel) {
                if (!visible) continue

                scissor(rectangle, scale) {
                    childPanel.drawPanel(mouseX, mouseY, scale)
                }

            } else {
                childPanel.drawPanel(mouseX, mouseY, scale)
            }
        }
    }

    override fun draw(mouseX: Int, mouseY: Int, scale: Int) {
        if (visible) {
            Rectangle(
                rectangle.left,
                rectangle.bottom - 1,
                rectangle.right,
                rectangle.bottom
            ).drawRect(Color.gray.rgb)

            textLabel.rectangle = Rectangle.fromWidthHeight(
                rectangle.left - scrollOffset,
                rectangle.top,
                getScaledStringWidth(text),
                rectangle.height
            )
            textLabel.text = text

            val cursorX = (rectangle.left + getScaledStringWidth(
                text.substring(
                    0,
                    cursorPosition
                )
            )).compress(max = rectangle.right)
            val y = mid(rectangle.top, rectangle.bottom) - (getScaledFontHeight() / 2)

            if (isFocused && (cursorCounter / 2) % 6 == 0) {

                val cursorRect = Rectangle(
                    cursorX,
                    (y - 1).compress(rectangle.top),
                    cursorX + 1,
                    (y + getScaledFontHeight() + 1).compress(max = rectangle.bottom)
                )

                GL11.glDisable(GL11.GL_SCISSOR_TEST)

                cursorRect.drawRect(0xFFD0D0D0.toInt())

                GL11.glEnable(GL11.GL_SCISSOR_TEST)
            }


            if (selectedText.isNotEmpty()) {
                val left = getScaledStringWidth(text.substring(0, selectedStart)) + rectangle.left
                val top = (y - 1).compress(0)
                val right = left + getScaledStringWidth(selectedText)
                val bottom =
                    (y + getScaledFontHeight() + 1).compress(max = rectangle.bottom)

                drawSelectedText(Rectangle(left, top, right, bottom))
            }
        }
    }

    private fun getScaledFontHeight() = (fontRenderer.FONT_HEIGHT * textScale).roundToInt()

    private fun getScaledStringWidth(s: String) = (fontRenderer.getStringWidth(s) * textScale).roundToInt()

    private fun drawSelectedText(selectedTextRect: Rectangle) {
        color(0f, 0f, 1f, 1f)
        disableTexture2D()
        disableDepth()
        enableColorLogic()
        colorLogicOp(GL11.GL_OR_REVERSE)

        val wr = tessellator.worldRenderer

        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
        wr.pos(selectedTextRect.left, selectedTextRect.top, 0).endVertex()
        wr.pos(selectedTextRect.left, selectedTextRect.bottom, 0).endVertex()
        wr.pos(selectedTextRect.right, selectedTextRect.bottom, 0).endVertex()
        wr.pos(selectedTextRect.right, selectedTextRect.top, 0).endVertex()
        tessellator.draw()

        disableColorLogic()
        enableTexture2D()
        enableDepth()
    }


}