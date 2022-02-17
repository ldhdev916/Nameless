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

package com.happyandjust.nameless.processor.experimantation

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.impl.skyblock.ExperimentationTableHelper
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import com.happyandjust.nameless.processor.Processor
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiScreenEvent
import java.awt.Color

object UltraSequencerProcessor : Processor() {

    private var ultrasequencerOrders = listOf<Int>()
    override val filter
        get() = ExperimentationTableHelper.processors[this]!!

    init {
        request<GuiScreenEvent.BackgroundDrawnEvent>().subscribe {
            gui.withInstance<GuiChest> {
                val containerChest = inventorySlots as ContainerChest
                val slots =
                    containerChest.inventorySlots.filter { it.inventory != com.happyandjust.nameless.dsl.mc.thePlayer.inventory }

                val item = slots[49].stack?.item

                if (item == Item.getItemFromBlock(Blocks.glowstone)) {
                    ultrasequencerOrders =
                        slots.filter { it.stack?.displayName?.stripControlCodes()?.matches("\\d+".toRegex()) == true }
                            .sortedBy { it.stack.stackSize }
                            .map { it.slotNumber }
                } else if (item == Items.clock) {
                    drawOrders(this)
                }
            }
        }
    }

    private fun drawOrders(gui: GuiChest) {
        matrix {

            val left = (gui as AccessorGuiContainer).guiLeft
            val top = gui.guiTop

            for ((index, slot) in ultrasequencerOrders.map { gui.inventorySlots.getSlot(it) }
                .filter { it.stack?.item == Item.getItemFromBlock(Blocks.stained_glass_pane) }.withIndex()) {

                if (index == 0) {
                    gui.drawOnSlot(slot, Color.yellow.rgb)
                }

                translate(left + slot.xDisplayPosition + 8.5, top + slot.yDisplayPosition + 8.5, 0.0) {
                    val text = (index + 1).toString()
                    mc.fontRendererObj.drawCenteredString(
                        text,
                        0xFFFF0000.toInt()
                    )
                }
            }
        }
    }
}