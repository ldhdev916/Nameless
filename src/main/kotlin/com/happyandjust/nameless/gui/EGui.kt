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

import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.gui.elements.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

class EGui : GuiScreen() {

    lateinit var backgroundRectangle: Rectangle
    var scale = 1.0
    var wholeScale = 2
    lateinit var searchButton: ESearchButton
    lateinit var categoryPanel: ECategoryPanel
    lateinit var featurePanel: EFeaturePanel
    lateinit var panelStacks: EPanelStacks
    lateinit var window: EWindow

    /**
     * Taken from Skyblock-Dungeons-Guide under AGPL-3.0 License
     *
     * Modified
     *
     * https://github.com/Dungeons-Guide/Skyblock-Dungeons-Guide/blob/master/LICENSE
     *
     * @author syeyoung
     */
    private fun determineScale() {

        wholeScale = 2

        val dw = mc.displayWidth
        val dh = mc.displayHeight

        val width = (dw * 0.84).toInt().compress(1250, 1500)
        val height = (dh * 0.77).toInt().compress(600, 800)

        if (dw <= width || dh <= height) {
            wholeScale /= 2
        }
    }

    override fun initGui() {

        Keyboard.enableRepeatEvents(true)

        determineScale()

        val dw = mc.displayWidth / wholeScale
        val dh = mc.displayHeight / wholeScale

        backgroundRectangle = Rectangle(
            (dw * 0.08).toInt(),
            (dh * 0.115).toInt(),
            (dw * 0.92).toInt(),
            (dh * 0.885).toInt()
        )

        window = EWindow(backgroundRectangle)

        window.addChild(ECategoryPanel(
            Rectangle.fromWidthHeight(
                backgroundRectangle.left,
                backgroundRectangle.top + window.titleOffset,
                backgroundRectangle.width / 7,
                backgroundRectangle.height - window.titleOffset
            )
        ).also { categoryPanel = it })

        window.addChild(ESearchButton(
            Rectangle.fromWidthHeight(
                backgroundRectangle.right - window.titleOffset,
                backgroundRectangle.top,
                window.titleOffset,
                window.titleOffset
            ),
            backgroundRectangle.width / 6,
            1.3
        ).also { searchButton = it })

        panelStacks = EPanelStacks(
            Rectangle.fromWidthHeight(
                categoryPanel.rectangle.right,
                categoryPanel.rectangle.top,
                backgroundRectangle.width - categoryPanel.rectangle.width,
                categoryPanel.rectangle.height
            )
        )

        window.addChild(panelStacks)

        //
        featurePanel = EFeaturePanel(
            panelStacks.rectangle,
            panelStacks
        )

        panelStacks.push(featurePanel)
        //

        //
        categoryPanel.clickListener = {
            it as ECategory
            featurePanel.filter { feature -> feature.category == it.category }

            panelStacks.clear()
            panelStacks.push(featurePanel)
        }
        //

        //
        searchButton.onTextFieldStateChange = {
            if (it) {
                when (val panel = panelStacks.peek()) {
                    is EFeaturePanel -> panel.filter { true }
                    is EFeatureSettingPanel -> panel.filter { true }
                    is EParameterSettingPanel -> panel.filter { true }
                }
            }
        }

        searchButton.onTextFieldKeyTyped = {
            when (val panel = panelStacks.peek()) {
                is EFeaturePanel -> panel.filter { feature ->
                    it.isEmpty() ||
                            feature.title.contains(it, true) ||
                            feature.desc.contains(it, true)
                }
                is EFeatureSettingPanel -> panel.filter { parameter ->
                    it.isEmpty() ||
                            parameter.title.contains(it, true) ||
                            parameter.desc.contains(it, true)
                }
                is EParameterSettingPanel -> panel.filter { parameter ->
                    it.isEmpty() ||
                            parameter.title.contains(it, true) ||
                            parameter.desc.contains(it, true)
                }
            }
        }

        searchButton.textFieldShouldNotBeFocusedByKeyType = { checkForFocusedTextFields(window) }
        //

        panelStacks.onStateChange = {
            searchButton.textField.text = ""
        }

        //
    }

    private fun checkForFocusedTextFields(vararg panels: EPanel): Boolean {
        for (panel in panels) {
            if (panel is ETextField && panel.isFocused) {
                return true
            }

            if (checkForFocusedTextFields(*panel.childPanels.toTypedArray())) {
                return true
            }
        }
        return false
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        drawDefaultBackground()

        val mouseX = Mouse.getEventX() / wholeScale
        val mouseY = (mc.displayHeight - Mouse.getEventY()) / wholeScale

        this.scale = 1.0 / ScaledResolution(mc).scaleFactor

        matrix {
            scale(scale * wholeScale, scale * wholeScale, 1.0)

            window.drawPanel(mouseX, mouseY, wholeScale)

            color(1f, 1f, 1f, 1f)

            categoryPanel.rectangle.copy(left = categoryPanel.rectangle.right - 3).drawRect(0xFF1E1E1E.toInt())
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {

        val mouseX = Mouse.getEventX() / wholeScale
        val mouseY = (mc.displayHeight - Mouse.getEventY()) / wholeScale

        window.mousePressed0(mouseX, mouseY)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {

        val mouseX = Mouse.getEventX() / wholeScale
        val mouseY = (mc.displayHeight - Mouse.getEventY()) / wholeScale

        window.mouseReleased0(mouseX, mouseY)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (!searchButton.textField.isFocused) { // need to intercept ESC
            super.keyTyped(typedChar, keyCode)
        }

        window.keyTyped0(typedChar, keyCode)
    }

    override fun updateScreen() {
        window.onUpdateScreen0()
    }

}
