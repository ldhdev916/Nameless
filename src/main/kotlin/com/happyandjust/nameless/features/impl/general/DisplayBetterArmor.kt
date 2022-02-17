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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.drawOnSlot
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.color
import com.happyandjust.nameless.features.scale
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import gg.essential.elementa.utils.withAlpha
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.GuiScreenEvent
import java.awt.Color
import kotlin.math.pow

object DisplayBetterArmor : SimpleFeature(
    "displayBetterArmor",
    "Display Better Armor",
    "In SkyWars, if there's a better armor than a one you're equipping, Draw box on item if it's in your inventory, make bigger if it's in ground. If mutltiple, only show the highest"
) {

    @JvmStatic
    var scaleJVM
        get() = scale
        set(value) {
            scale = value
        }

    @JvmStatic
    val enabledJVM
        get() = enabled

    init {
        parameter(Color.green.withAlpha(80).toChromaColor()) {
            matchKeyCategory()
            key = "color"
            title = "Inventory Box Color"
        }

        parameter(3.0) {
            matchKeyCategory()
            key = "scale"
            title = "Dropped Item Scale"

            settings {
                ordinal = 1

                minValue = 1.5
                maxValue = 7.0
            }
        }
    }

    private val scanTimer = TickTimer(7)
    private val drawSlots = arrayListOf<Slot>()

    @JvmField
    val scaledItems = arrayListOf<EntityItem>()

    private fun ItemStack.getFinalDamage() = calcDamage(item as ItemArmor, getProtectionLevel(this))

    private fun getProtectionLevel(itemStack: ItemStack): Int {
        val enchantments = itemStack.enchantmentTagList ?: return 0

        return (0 until enchantments.tagCount())
            .map { enchantments.getCompoundTagAt(it) }
            .firstOrNull { it.getInteger("id") == 0 }
            ?.getInteger("lvl") ?: 0
    }

    private fun calcDamage(itemArmor: ItemArmor, protectionLevel: Int): Double {
        var testDamage = 1000.0

        testDamage *= (25 - itemArmor.damageReduceAmount) // armor reduction

        testDamage /= 25

        if (protectionLevel == 0) return testDamage

        // enchantment
        val modifier = MathHelper.floor_float(((6 + protectionLevel.toFloat().pow(2)) / 3F) * 0.75F).coerceIn(0, 25)

        // wtf minecraft uses random here
        var k = (modifier + 1 shr 1) + (((modifier shr 1) + 1) / 2)

        k = k.coerceAtMost(20)

        if (k > 0) {
            val l = 25 - k
            val f1 = testDamage * l
            testDamage = f1 / 25F
        }

        return testDamage

    }

    init {
        on<SpecialTickEvent>()
            .filter { enabled && Hypixel.currentGame == GameType.SKYWARS }
            .filter { scanTimer.update().check() }.subscribe {
                scanInventory()
                scanEntityItem()
            }
    }

    private fun scanForBetterItem(itemsToScan: Iterable<ItemStack>): List<ItemStack> {
        val armorInventory = mc.thePlayer.inventory.armorInventory.filter { it?.item is ItemArmor }
            .map { it.getFinalDamage() to it.item as ItemArmor }
        val list = arrayListOf<ItemStack>()

        val actualScanItems = itemsToScan.filter { it.item is ItemArmor }
            .groupBy { (it.item as ItemArmor).armorType }

        for ((finalDamage, itemArmor) in armorInventory) {
            val items = actualScanItems[itemArmor.armorType]?.takeIf { it.isNotEmpty() } ?: continue

            val itemStackByFinalDamage = items.map { it to it.getFinalDamage() }.minByOrNull { it.second }!!

            if (itemStackByFinalDamage.second < finalDamage) {
                list.add(itemStackByFinalDamage.first)
            }
        }

        return list
    }

    private fun scanEntityItem() {
        val items = mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>()
            .associateBy { it.entityItem }
        scaledItems.clear()
        scaledItems.addAll(scanForBetterItem(items.keys).mapNotNull { items[it] })
    }

    private fun scanInventory() {
        val gui = mc.currentScreen

        if (gui !is GuiInventory) {
            drawSlots.clear()
            return
        }

        val items = gui.inventorySlots.inventorySlots
            .filter { it.inventory == mc.thePlayer.inventory && it.slotIndex !in 36 until 40 && it.hasStack }
            .associateBy { it.stack }

        drawSlots.clear()
        drawSlots.addAll(scanForBetterItem(items.keys).mapNotNull { items[it] })
    }

    init {
        on<GuiScreenEvent.BackgroundDrawnEvent>().filter { enabled && Hypixel.currentGame == GameType.SKYWARS }
            .subscribe {
                gui.withInstance<GuiInventory> {
                    val color = color.rgb
                    for (slot in drawSlots) {
                        drawOnSlot(slot, color)
                    }
                }
            }
    }

}