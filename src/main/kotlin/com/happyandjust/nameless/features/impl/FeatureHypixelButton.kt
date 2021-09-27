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

package com.happyandjust.nameless.features.impl

import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ScreenActionPerformedListener
import com.happyandjust.nameless.features.listener.ScreenInitListener
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiScreenEvent

class FeatureHypixelButton : SimpleFeature(
    Category.QOL,
    "hypixelbutton",
    "Add Join Hypixel Button",
    "Add join hypixel button in main menu instead of realm button",
    true
), ScreenInitListener, ScreenActionPerformedListener {

    override fun actionPerformedPre(e: GuiScreenEvent.ActionPerformedEvent.Pre) {

    }

    override fun actionPerformedPost(e: GuiScreenEvent.ActionPerformedEvent.Post) {
        if (e.button.id == 10001 && e.gui is GuiMainMenu && enabled) {
            val guiConnecting = GuiConnecting(e.gui, mc, ServerData("Hypixel", "stuck.hypixel.net", false))
            mc.displayGuiScreen(guiConnecting)
        }
    }

    override fun screenInitPre(e: GuiScreenEvent.InitGuiEvent.Pre) {

    }

    override fun screenInitPost(e: GuiScreenEvent.InitGuiEvent.Post) {
        if (e.gui is GuiMainMenu && enabled) {
            for (button in e.buttonList) {
                if (button.id == 14) { // Realm button
                    button.id = 10001
                    button.displayString = "Hypixel"
                }
            }
        }
    }
}
