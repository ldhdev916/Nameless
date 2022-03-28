package com.happyandjust.nameless.hypixel.skyblock.experimentation

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.Item
import net.minecraft.network.play.client.C0EPacketClickWindow
import net.minecraftforge.client.event.GuiScreenEvent.BackgroundDrawnEvent

class Chronomatron : ExperimentationGame {

    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val patterns = arrayListOf<String>()
    private var totalClicks = 0
    private var lastRound = 0

    override fun registerEventListeners() {
        on<SpecialTickEvent>().addSubscribe {
            withInstance<GuiChest>(mc.currentScreen) {
                val containerChest = inventorySlots as ContainerChest
                val slots = containerChest.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }

                val timer = slots.getOrNull(49)?.stack ?: return@addSubscribe

                when (timer.item) {
                    Items.clock -> {
                        val displayName = timer.displayName.stripControlCodes()
                        timerPattern.matchesMatcher(displayName) {
                            val round = slots[4].stack?.stackSize ?: return@addSubscribe
                            val remainSecond = group("sec").toInt()

                            if (round != lastRound && remainSecond == round + 2) {
                                lastRound = round

                                val colorName = slots.first { it.stack?.item == stainedHardenedClay }.stack.displayName
                                patterns.add(colorName)
                            }
                        }
                    }
                    glowstone -> totalClicks = 0
                }
            }
        }

        on<PacketEvent.Sending>().addSubscribe {
            withInstance<C0EPacketClickWindow>(packet) {
                withInstance<GuiChest>(mc.currentScreen) {
                    val stack = inventorySlots.inventorySlots.getOrNull(49)?.stack ?: return@addSubscribe
                    val displayName by lazy { stack.displayName.stripControlCodes() }

                    if (stack.item == Items.clock && displayName.matches(timerPattern.toRegex()) && clickedItem?.item in clickableBlocks) {
                        totalClicks++
                    }
                }
            }
        }

        on<BackgroundDrawnEvent>().addSubscribe {
            withInstance<GuiChest>(gui) {
                withInstance<AccessorGuiContainer>(gui) {
                    val pattern = patterns.getOrNull(totalClicks) ?: return@addSubscribe
                    val color = mc.fontRendererObj.getColorCode(pattern[1]).withAlpha(1f)

                    val slots = inventorySlots.inventorySlots.filter { it.inventory != mc.thePlayer.inventory }

                    slots.filter { it.stack?.displayName == pattern }.forEach {
                        drawOnSlot(it, color)
                    }

                    matrix {
                        translate(guiLeft - PADDING, guiTop)
                        var y = 0.0

                        with(mc.fontRendererObj) {
                            for (colorName in patterns.drop(totalClicks)) {
                                drawString(pattern, -getStringWidth(pattern), y, dropShadow = true)

                                y += FONT_HEIGHT * 1.5
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {

        private const val PADDING = 10

        private val timerPattern = "Timer: (?<sec>\\d+)s".toPattern()
        private val stainedHardenedClay by lazy { Item.getItemFromBlock(Blocks.stained_hardened_clay) }
        private val glowstone by lazy { Item.getItemFromBlock(Blocks.glowstone) }

        private val clickableBlocks by lazy {
            setOf(Item.getItemFromBlock(Blocks.stained_glass), stainedHardenedClay)
        }
    }
}