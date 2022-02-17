/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2022 HappyAndJust
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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.features.base.SimpleFeature
import gg.essential.api.utils.GuiUtil
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiScreenEvent

object AddHypixelButton : SimpleFeature(
    "hypixelButton",
    "Add Join Hypixel Button",
    "Add join hypixel button in main menu instead of realm button",
    true
) {

    init {
        on<GuiScreenEvent.ActionPerformedEvent.Post>().filter { button.id == 10001 && gui is GuiMainMenu && enabled }
            .subscribe {
                GuiUtil.open(GuiConnecting(gui, mc, ServerData("Hypixel", "stuck.hypixel.net", false)))
            }

        on<GuiScreenEvent.InitGuiEvent.Post>().filter { gui is GuiMainMenu && enabled }.subscribe {
            buttonList.find { it.id == 14 }?.apply {
                id = 10001
                displayString = "Hypixel"
            }
        }
    }
}