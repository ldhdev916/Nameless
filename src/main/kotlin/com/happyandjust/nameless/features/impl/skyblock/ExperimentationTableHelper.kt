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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.stripControlCodes
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.processor.experimantation.ChronomatronProcessor
import com.happyandjust.nameless.processor.experimantation.SuperpairsProcessor
import com.happyandjust.nameless.processor.experimantation.UltraSequencerProcessor
import com.happyandjust.nameless.serialization.converters.CList
import com.happyandjust.nameless.serialization.converters.getEnumConverter
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.client.event.GuiOpenEvent

object ExperimentationTableHelper :
    SimpleFeature(Category.SKYBLOCK, "experimentationtablehelper", "Experimentation Table Helper", "") {

    private fun checkForRequirement() = enabled && Hypixel.currentGame == GameType.SKYBLOCK
    private var currentExperimentationType: ExperimentationType? = null
    val processors = hashMapOf<Processor, () -> Boolean>()

    private var selectedExperimentationType by object :
        FeatureParameter<List<ExperimentationType>>(
            0,
            "experimentationhelper",
            "types",
            "Experimentation Types",
            "",
            ExperimentationType.values().toList(),
            CList(
                getEnumConverter()
            )
        ) {

        init {
            allEnumList = ExperimentationType.values().toList()
            enumName = {
                it as ExperimentationType
                val name = it.name

                "${name[0]}${name.drop(1).lowercase()}"
            }
            for (experimentationType in ExperimentationType.values()) {
                processors[experimentationType.processor] =
                    { checkForRequirement() && mc.currentScreen is GuiChest && currentExperimentationType == experimentationType && experimentationType in value }
            }
        }

        override fun getComponentType() = ComponentType.MULTI_SELECTOR
    }

    init {
        on<SpecialTickEvent>().filter {
            checkForRequirement().also { if (!it) currentExperimentationType = null }
        }.subscribe {
            mc.currentScreen.withInstance<GuiChest> {
                val containerChest = inventorySlots as ContainerChest
                val displayName = containerChest.lowerChestInventory.displayName.unformattedText.stripControlCodes()
                val size =
                    containerChest.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }.size

                currentExperimentationType = when {
                    size != 54 -> null
                    displayName.startsWith("Chronomatron (") -> ExperimentationType.CHRONOMATRON
                    displayName.startsWith("Ultrasequencer (") -> ExperimentationType.ULTRASEQUENCER
                    displayName.startsWith("Superpairs (") -> ExperimentationType.SUPERPAIRS
                    else -> null
                }
                return@subscribe
            }
            currentExperimentationType = null
        }

        on<GuiOpenEvent>().subscribe {
            ChronomatronProcessor.chronomatronClicks = 0
            ChronomatronProcessor.chronomatronPatterns.clear()
            ChronomatronProcessor.lastRound = 0

            SuperpairsProcessor.itemBySlotNumber.clear()
        }
    }

    enum class ExperimentationType(val processor: Processor) {
        CHRONOMATRON(ChronomatronProcessor), ULTRASEQUENCER(UltraSequencerProcessor), SUPERPAIRS(SuperpairsProcessor)
    }
}