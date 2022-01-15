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

package com.happyandjust.nameless.processor.experimantation

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.impl.skyblock.ExperimentationTableHelper
import com.happyandjust.nameless.processor.Processor
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import java.awt.Color

/**
 * Taken from Danker's Skyblock Mod under GPL-3.0 License
 *
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
object SuperpairsProcessor : Processor() {

    val itemBySlotNumber = hashMapOf<Int, ItemStack>()
    private val colors = arrayOf(
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
    override val filter
        get() = ExperimentationTableHelper.processors[this]!!

    init {
        request<GuiScreenEvent.BackgroundDrawnEvent>().subscribe {
            gui.withInstance<GuiChest> {
                val containerChest = inventorySlots as ContainerChest
                val slots = containerChest.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }

                slots.filter {
                    it.hasStack && it.stack.item !in
                            Item.getItemFromBlock(Blocks.stained_glass) to
                            Item.getItemFromBlock(Blocks.stained_glass_pane) &&
                            arrayOf("Instant Find", "Gained +").none(it.stack.displayName::contains) &&
                            it.slotNumber !in itemBySlotNumber
                }
                    .forEach {
                        itemBySlotNumber[it.slotNumber] =
                            it.stack.copy().setStackDisplayName(it.stack.displayName.run {
                                if ("Enchanted Book" in this)
                                    it.stack.getTooltip(mc.thePlayer, false)[3]
                                else if (it.stack.stackSize > 1) "${it.stack.stackSize} $this"
                                else this
                            })
                    }

                fun ItemStack.keyName() = "$displayName$unlocalizedName"

                val matches = itemBySlotNumber.entries.groupBy({ it.value.keyName() }) { it.key }

                var currentIndex = 0

                val getColor = { colors[currentIndex % colors.size] }

                matches.values
                    .toSet()
                    .filter { it.size >= 2 }
                    .forEach {
                        it.forEach { index ->
                            drawOnSlot(containerChest.getSlot(index), getColor().rgb)
                        }
                        currentIndex++
                    }

            }
        }

        request<ItemTooltipEvent>().subscribe {
            mc.currentScreen.withInstance<GuiChest> {
                if (arrayOf(
                        "Click any button",
                        "Click a second button",
                        "Next button is instantly rewarded",
                        "Stained Glass"
                    ).any { itemStack.displayName.contains(it) }
                ) {
                    val target = itemBySlotNumber[slotUnderMouse.slotNumber] ?: return@subscribe
                    val itemName = target.displayName

                    if (toolTip.any { it.stripControlCodes() == itemName.stripControlCodes() }) return@subscribe
                    toolTip.removeIf {
                        it.stripControlCodes()
                            .let { strip -> strip == "minecraft:stained_glass" || strip.startsWith("NBT: ") }
                    }
                    toolTip.add(itemName)
                    toolTip.add(target.item.registryName)
                }
            }
        }
    }
}