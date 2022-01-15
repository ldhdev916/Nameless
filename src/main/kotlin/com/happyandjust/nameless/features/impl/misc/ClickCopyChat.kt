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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.mixins.accessors.AccessorGuiNewChat
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.GuiNewChat
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.IChatComponent
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.GuiScreenEvent
import org.lwjgl.input.Mouse
import kotlin.math.min

object ClickCopyChat : SimpleFeature(
    Category.MISCELLANEOUS,
    "clickcopychat",
    "Click Copy Chat",
    "Right click to copy chat, ctrl + right click to copy chat with color codes"
) {

    init {
        on<GuiScreenEvent.MouseInputEvent.Pre>().filter { enabled && Mouse.getEventButton() == 1 && Mouse.getEventButtonState() && gui is GuiChat }
            .subscribe {
                val isCtrlDown = GuiScreen.isCtrlKeyDown()

                val chat = mc.ingameGUI.chatGUI.getFullChatComponent(Mouse.getX(), Mouse.getY()) ?: return@subscribe

                (if (isCtrlDown) chat.formattedText else chat.unformattedText.stripControlCodes()).copyToClipboard()
                sendPrefixMessage("Chat copied to your clipboard")

                isCanceled = true
            }
    }

    // I hate minecraft's code
    private fun GuiNewChat.getFullChatComponent(mouseX: Int, mouseY: Int): IChatComponent? {
        val factor = ScaledResolution(mc).scaleFactor
        val mouseX = MathHelper.floor_float((mouseX / factor - 3) / chatScale)
        val mouseY = MathHelper.floor_float((mouseY / factor - 27) / chatScale)

        if (mouseX < 0 || mouseY < 0) return null

        val lineCount = min(lineCount, (this as AccessorGuiNewChat).drawnChatLines.size)

        if (mouseX <= MathHelper.floor_float(chatWidth / chatScale) && mouseY < mc.fontRendererObj.FONT_HEIGHT * lineCount + lineCount) {
            val chatY = mouseY / mc.fontRendererObj.FONT_HEIGHT + scrollPos

            if (chatY in 0 until drawnChatLines.size) {
                return drawnChatLines[chatY].chatComponent.takeIf { mouseX <= mc.fontRendererObj.getStringWidth(it.unformattedText) }
            }
        }

        return null
    }
}