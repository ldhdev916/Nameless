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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ChatListener
import com.happyandjust.nameless.features.listener.ScreenMouseInputListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorGuiScreen
import net.minecraft.client.gui.GuiChat
import net.minecraft.event.ClickEvent
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiScreenEvent
import org.lwjgl.input.Mouse
import java.util.*
import kotlin.concurrent.timerTask

object FeatureClickOpenSlayer :
    SimpleFeature(Category.SKYBLOCK, "clickopenslayer", "Click Anywhere to Open Slayer Menu"), ChatListener,
    ScreenMouseInputListener {

    private var openMenuComponent: IChatComponent? = null
        set(value) {
            field = value
            if (value != null) {
                Timer().schedule(timerTask {
                    if (field == value) {
                        field = null
                    }
                }, 10000L)
            }
        }

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (enabled && Hypixel.currentGame == GameType.SKYBLOCK && e.type.toInt() != 2) {
            openMenuComponent = e.message.siblings.find {
                it.unformattedText == "§2§l[OPEN MENU]" && it.chatStyle?.chatClickEvent?.run {
                    action == ClickEvent.Action.RUN_COMMAND && value.startsWith("/cb")
                } == true
            }
        }
    }

    override fun onMouseInputPre(e: GuiScreenEvent.MouseInputEvent.Pre) {

    }

    override fun onMouseInputPost(e: GuiScreenEvent.MouseInputEvent.Post) {
        val gui = e.gui
        if (enabled && Mouse.getEventButton() == 0 && gui is GuiChat && gui is AccessorGuiScreen && Mouse.getEventButtonState()) {
            gui.invokeHandleComponentClick(openMenuComponent)
        }
    }
}