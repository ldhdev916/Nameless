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

import com.happyandjust.nameless.VERSION
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.FMLCommonHandler
import java.awt.Color
import java.awt.Desktop
import java.net.URI

class GuiError(private val parent: GuiMainMenu, private val reason: String) : GuiScreen() {

    override fun initGui() {

        val h = (height * 0.7).toInt()

        buttonList.add(GuiButton(0, width / 2 - 100, h, "Exit Minecraft"))
        buttonList.add(GuiButton(1, width / 2 - 100, h + 30, "Play Without Nameless $VERSION"))
        buttonList.add(GuiButton(2, width / 2 - 100, h + 60, "Download Latest Nameless"))
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> FMLCommonHandler.instance().exitJava(0, false)
            1 -> mc.displayGuiScreen(parent)
            2 -> try {
                Desktop.getDesktop().browse(URI("https://github.com/HappyAndJust/Nameless/releases/latest"))
            } catch (ignored: Exception) {

            }
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {

        drawBackground(1)
        super.drawScreen(mouseX, mouseY, partialTicks)

        val text1 = "Current Version of Nameless $VERSION is regarded causing errors."
        val text2 = "Reason: $reason"
        val FONT_HEIGHT = fontRendererObj.FONT_HEIGHT

        GlStateManager.pushMatrix()

        fontRendererObj.drawString(
            text1,
            (width / 2) - (fontRendererObj.getStringWidth(text1) / 2),
            (height / 2) - (FONT_HEIGHT / 2),
            Color.red.rgb
        )
        fontRendererObj.drawString(
            text2,
            (width / 2) - (fontRendererObj.getStringWidth(text2) / 2),
            (height / 2) + (FONT_HEIGHT / 2),
            Color.red.rgb
        )

        GlStateManager.popMatrix()
    }
}