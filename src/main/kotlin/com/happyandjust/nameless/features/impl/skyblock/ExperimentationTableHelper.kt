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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.SkyBlock
import com.happyandjust.nameless.hypixel.skyblock.experimentation.ExperimentationGame
import com.happyandjust.nameless.hypixel.skyblock.experimentation.ExperimentationType
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest

object ExperimentationTableHelper : SimpleFeature("experimentationTableHelper", "Experimentation Table Helper", "") {

    init {
        hierarchy { +::types }
    }

    private var currentExperimentationGame: ExperimentationGame? = null
        set(value) {
            field?.unregisterAll()
            field = value
        }
    private val scanTimer = TickTimer.withSecond(0.5)

    private var types by parameter(listEnum<ExperimentationType>()) {
        key = "types"
        title = "Supported Experimentation Games"

        settings {
            autoFillEnum { it.chestDisplayName }
        }
    }

    init {
        on<SpecialTickEvent>().timerFilter(scanTimer).subscribe {
            if (!enabled || Hypixel.currentGame !is SkyBlock) {
                currentExperimentationGame = null
                return@subscribe
            }
            withInstance<GuiChest>(mc.currentScreen) {
                val containerChest = inventorySlots as ContainerChest
                val displayName = containerChest.lowerChestInventory.displayName.unformattedText.stripControlCodes()
                val size =
                    containerChest.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }.size

                if (size != 54) return@withInstance
                val currentGame = getCurrentGame(displayName) ?: return@withInstance
                if (currentExperimentationGame?.javaClass != currentGame) {
                    currentExperimentationGame = currentGame
                    currentGame.registerEventListeners()
                    sendDebugMessage("ExperimentationTableHelper", "Current Game: $currentGame")
                }

                return@subscribe
            }
            currentExperimentationGame = null
        }
    }

    private fun getCurrentGame(displayName: String) =
        types.find { displayName.startsWith("${it.chestDisplayName} (") }?.createImpl()
}