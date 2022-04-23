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

package com.happyandjust.nameless.hypixel.skyblock.experimentation

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import java.awt.Color

/**
 * Taken from Danker's Skyblock Mod under GPL-3.0 License
 *
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
class Superpairs : ExperimentationGame {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val stackBySlotNumber = hashMapOf<Int, ItemStack>()

    override fun registerEventListeners() {
        on<SpecialTickEvent>().addSubscribe {
            withInstance<GuiChest>(mc.currentScreen) {
                val slots = inventorySlots.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }

                slots.filterNot {
                    val stack = it.stack
                    val displayName by lazy { stack.displayName }
                    val filterNames by lazy { listOf("Instant Find", "Gained +") }

                    stack == null || stack.item in clickableItems || displayName in filterNames || it.slotNumber in stackBySlotNumber
                }.forEach {
                    val stack = it.stack
                    val displayName = stack.displayName

                    val itemName = when {
                        "Enchanted Book" in displayName -> stack.getTooltip(mc.thePlayer, false)[3]
                        stack.stackSize > 1 -> "${stack.stackSize} $displayName"
                        else -> displayName
                    }

                    stackBySlotNumber[it.slotNumber] = stack.copy().setStackDisplayName(itemName)
                }
            }
        }

        on<BackgroundDrawnEvent>().addSubscribe {
            withInstance<GuiChest>(gui) {
                val getKeyName: (ItemStack) -> String = {
                    "${it.displayName}${it.unlocalizedName}"
                }
                val matches = stackBySlotNumber.entries.groupBy({ getKeyName(it.value) }, { it.key })

                matches.values.toSet()
                    .filter { it.size >= 2 }
                    .forEachIndexed { index, slotIndexes ->
                        val color = colors[index % colors.size].rgb
                        slotIndexes.forEach {
                            drawOnSlot(inventorySlots.getSlot(it), color)
                        }
                    }
            }
        }

        on<ItemTooltipEvent>().addSubscribe {
            withInstance<GuiChest>(mc.currentScreen) {
                if (nameHelpers.any { it in itemStack.displayName }) {
                    val target = stackBySlotNumber[slotUnderMouse.slotNumber] ?: return@addSubscribe
                    val itemName = target.displayName

                    if (toolTip.any { it.stripControlCodes() == itemName.stripControlCodes() }) return@addSubscribe

                    toolTip.removeIf {
                        val strip = it.stripControlCodes()
                        strip == "minecraft:stained_glass" || strip.startsWith("NBT: ")
                    }
                    toolTip.add(itemName)
                    toolTip.add(target.item.registryName)
                }
            }
        }

    }

    companion object {

        private val nameHelpers = setOf(
            "Click any button",
            "Click a second button",
            "Next button is instantly rewarded",
            "Stained Glass"
        )

        private val clickableItems = setOf(
            Item.getItemFromBlock(Blocks.stained_glass),
            Item.getItemFromBlock(Blocks.stained_glass_pane)
        )

        private val colors = listOf(
            Color(255, 0, 0, 100),
            Color(0, 0, 255, 100),
            Color(100, 179, 113, 100),
            Color(255, 114, 255, 100),
            Color(255, 199, 87, 100),
            Color(119, 105, 198, 100),
            Color(135, 199, 112, 100),
            Color(240, 37, 240, 100),
            Color(178, 132, 190, 100),
            Color(63, 135, 163, 100),
            Color(146, 74, 10, 100),
            Color(255, 255, 255, 100),
            Color(217, 252, 140, 100),
            Color(255, 82, 82, 100)
        )
    }
}