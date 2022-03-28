package com.happyandjust.nameless.hypixel.skyblock.experimentation

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent
import java.awt.Color

class UltraSequencer : ExperimentationGame {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private var orderSlotIds = emptyList<Int>()

    override fun registerEventListeners() {
        on<SpecialTickEvent>().addSubscribe {
            withInstance<GuiChest>(mc.currentScreen) {
                val slots = inventorySlots.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }

                val glowStone = slots.getOrNull(49)?.stack?.item

                if (glowStone == Item.getItemFromBlock(Blocks.glowstone)) {
                    orderSlotIds = slots
                        .filter {
                            val displayName = it.stack?.displayName?.stripControlCodes() ?: return@filter false

                            displayName.matches(NUMBER)
                        }.sortedBy { it.stack.stackSize }
                        .map { it.slotNumber }
                }
            }
        }

        on<BackgroundDrawnEvent>().addSubscribe {
            withInstance<GuiChest>(gui) {
                withInstance<AccessorGuiContainer>(gui) {
                    val slots = inventorySlots.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }

                    val clock = slots.getOrNull(49)?.stack?.item

                    if (clock == Items.clock) {
                        val filtered = orderSlotIds.map { inventorySlots.getSlot(it) }
                            .filter { it.stack?.item == stainedGlassPane }

                        matrix {
                            for ((index, slot) in filtered.withIndex()) {
                                if (index == 0) {
                                    drawOnSlot(slot, Color.yellow.rgb)
                                }

                                translate(guiLeft + slot.xDisplayPosition + 8.5, guiTop + slot.yDisplayPosition + 8.5) {
                                    mc.fontRendererObj.drawCenteredString((index + 1).toString(), Color.red.rgb)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {

        private val NUMBER = "\\d+".toRegex()
        private val stainedGlassPane by lazy { Item.getItemFromBlock(Blocks.stained_glass_pane) }
    }
}