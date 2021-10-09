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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.devqol.LOGGER
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ScreenActionPerformedListener
import com.happyandjust.nameless.features.listener.ScreenInitListener
import com.happyandjust.nameless.mixins.accessors.AccessorGuiDisconnected
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiScreenEvent

class FeatureReconnectButton : SimpleFeature(
    Category.QOL,
    "reconnectbutton",
    "Add Reconnect Button",
    "Add reconnect button when you got disconnected",
    true
), ScreenInitListener, ScreenActionPerformedListener {

    private var lastServerData: ServerData? = null

    override fun actionPerformedPre(e: GuiScreenEvent.ActionPerformedEvent.Pre) {

    }

    override fun actionPerformedPost(e: GuiScreenEvent.ActionPerformedEvent.Post) {
        val gui = e.gui
        if (e.button.id == 101 && gui is AccessorGuiDisconnected && enabled) {
            val guiConnecting = GuiConnecting(gui.parentScreen, mc, lastServerData!!)
            mc.displayGuiScreen(guiConnecting)
        }
    }

    override fun screenInitPre(e: GuiScreenEvent.InitGuiEvent.Pre) {
    }

    override fun screenInitPost(e: GuiScreenEvent.InitGuiEvent.Post) {
        when (e.gui) {
            is GuiDisconnected -> {
                if (enabled) {
                    if (lastServerData != null) { // This should not be happened

                        for (button in e.buttonList) {
                            if (button.id == 0) {
                                e.buttonList.add(
                                    GuiButton(
                                        101,
                                        button.xPosition,
                                        button.yPosition + button.height + 10,
                                        "Reconnect"
                                    )
                                )
                                break
                            }
                        }

                    } else {
                        LOGGER.error("ERROR: ServerData is null, how tf you got disconnected from server")
                    }
                }
            }
            is GuiConnecting -> lastServerData = mc.currentServerData
        }

    }
}
