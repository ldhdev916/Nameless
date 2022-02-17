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
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.impl.skyblock.ExperimentationTableHelper
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import com.happyandjust.nameless.processor.Processor
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.client.event.GuiScreenEvent

object ChronomatronProcessor : Processor() {

    private val timerPattern = "Timer: (?<sec>\\d+)s".toPattern()
    val chronomatronPatterns = arrayListOf<String>()
    var chronomatronClicks = 0
    var lastRound = 0
    override val filter
        get() = ExperimentationTableHelper.processors[this]!!

    init {
        request<GuiScreenEvent.BackgroundDrawnEvent>().subscribe {
            gui.withInstance<GuiChest> {
                val containerChest = inventorySlots as ContainerChest
                val slots =
                    containerChest.inventorySlots.filter { it.inventory != com.happyandjust.nameless.dsl.mc.thePlayer.inventory }

                val timerItem = slots[49].stack
                val item = timerItem?.item ?: return@subscribe

                if (item == Items.clock) {
                    timerPattern.matchesMatcher(timerItem.displayName.stripControlCodes()) {

                        val round = slots[4].stack?.stackSize ?: return@subscribe

                        if (round != lastRound && group("sec").toInt() == round + 2) {
                            lastRound = round

                            val colorName =
                                slots.firstOrNull { it.stack?.item == Item.getItemFromBlock(Blocks.stained_hardened_clay) }?.stack?.displayName
                                    ?: return@subscribe

                            chronomatronPatterns.add(colorName)
                        }


                        if (chronomatronClicks in 0 until chronomatronPatterns.size) {
                            val pattern = chronomatronPatterns[chronomatronClicks]
                            slots.filter { it.stack?.displayName == pattern }.forEach {
                                val color =
                                    com.happyandjust.nameless.dsl.mc.fontRendererObj.getColorCode(pattern[1]) or 0xFF000000.toInt()
                                drawOnSlot(it, color)
                            }

                            drawTexts(this@withInstance)
                        }
                    }
                } else if (item == Item.getItemFromBlock(Blocks.glowstone)) {
                    chronomatronClicks = 0
                }
            }
        }
    }

    private fun drawTexts(gui: GuiChest) {
        val left = (gui as AccessorGuiContainer).guiLeft
        val top = gui.guiTop

        val padding = 10

        matrix {
            var y = 0
            translate(left - padding, top, 0)

            for (pattern in chronomatronPatterns.subList(chronomatronClicks, chronomatronPatterns.size)) {
                mc.fontRendererObj.drawString(
                    pattern,
                    -mc.fontRendererObj.getStringWidth(pattern),
                    y,
                    0xFFFFFFFF.toInt(),
                    true
                )

                y += (mc.fontRendererObj.FONT_HEIGHT * 1.5).toInt()
            }
        }
    }

    init {
        request<PacketEvent.Sending>().subscribe {
            packet.withInstance<C0EPacketClickWindow> {
                mc.currentScreen.withInstance<GuiChest> {
                    val item = clickedItem?.item

                    if (inventorySlots.inventorySlots[49].stack?.displayName?.stripControlCodes()
                            ?.matches(timerPattern.toRegex()) == true && item in Item.getItemFromBlock(Blocks.stained_glass) to Item.getItemFromBlock(
                            Blocks.stained_hardened_clay
                        )
                    ) {
                        chronomatronClicks++
                    }
                }
            }
        }
    }
}