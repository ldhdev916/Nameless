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
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority

object FeatureJoinHypixelImmediately : SimpleFeature(
    Category.QOL,
    "joinhypixelimmediately",
    "Join Hypixel Immediately",
    "Automatically joins hypixel when main menu is shown"
) {

    private var firstShown = true

    init {
        on<GuiOpenEvent>().apply {
            priority = EventPriority.LOWEST
        }.filter { gui is GuiMainMenu }.subscribe {
            if (firstShown) {
                firstShown = false
                if (enabled) {
                    gui = GuiConnecting(gui, mc, ServerData("Hypixel", "mc.hypixel.net", false))
                }
            }
        }
    }

}