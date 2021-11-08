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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.drawOnSlot
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.pow
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.BackgroundDrawnListener
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorNBTTagList
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.serialization.converters.CDouble
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.item.EntityItem
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.MathHelper
import net.minecraftforge.client.event.GuiScreenEvent
import java.awt.Color

object FeatureDisplayBetterArmor : SimpleFeature(
    Category.GENERAL,
    "displaybetterarmor",
    "Display Better Armor",
    "In SkyWars, if there's a better armor than a one you're equipping, Draw box on item if it's in your inventory, make bigger if it's in ground. If mutltiple, only show the highest"
), ClientTickListener, BackgroundDrawnListener {

    init {
        parameters["color"] = FeatureParameter(
            0,
            "betterarmor",
            "color",
            "Inventory Box Color",
            "",
            Color.green.toChromaColor(),
            CChromaColor
        )
        parameters["scale"] = FeatureParameter(
            0,
            "betterarmor",
            "scale",
            "Dropped Item Scale",
            "",
            3.0,
            CDouble
        ).also {
            it.minValue = 1.5
            it.maxValue = 7.0
        }
    }

    private var scanTick = 0

    private val drawSlots = arrayListOf<Slot>()
    val scaledItems = arrayListOf<EntityItem>()

    private operator fun ItemStack.compareTo(other: ItemStack): Int {
        val myItem = item
        val otherItem = other.item

        if (myItem !is ItemArmor || otherItem !is ItemArmor) throw RuntimeException("Can't compare Non-Armor item")

        val damage1 = calcDamage(myItem, getProtectionLevel(this))
        val damage2 = calcDamage(otherItem, getProtectionLevel(other))

        return damage2.compareTo(damage1)
    }

    private fun getProtectionLevel(itemStack: ItemStack): Int {
        val enchantments = itemStack.enchantmentTagList ?: return 0

        for (enchantment in (enchantments as AccessorNBTTagList).tagList) {
            if (enchantment is NBTTagCompound) {
                val id = enchantment.getInteger("id")
                if (id == 0) {
                    return enchantment.getInteger("lvl")
                }
            }
        }

        return 0
    }

    private fun calcDamage(itemArmor: ItemArmor, protectionLevel: Int): Double {
        var testDamage = 1000.0

        testDamage *= (25 - itemArmor.damageReduceAmount) // armor reduction

        testDamage /= 25

        if (protectionLevel == 0) return testDamage

        // enchantment
        val modifier = MathHelper.floor_float(((6 + protectionLevel.pow(2)) / 3F) * 0.75F).coerceIn(0, 25)

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

    override fun tick() {
        if (!enabled || Hypixel.currentGame != GameType.SKYWARS) return

        scanTick = (scanTick + 1) % 7

        if (scanTick != 0) return

        drawSlots.clear()
        scaledItems.clear()

        scanInventory()
        scanEntityItem()
    }

    private fun scanEntityItem() {

        val armorInventory = mc.thePlayer.inventory.armorInventory

        val armorItems =
            mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>().filter { it.entityItem.item is ItemArmor }

        for (armor in armorInventory) {
            armor ?: continue

            val armorType = (armor.item as? ItemArmor)?.armorType ?: continue

            var bestEntityItem: EntityItem? = null
            var bestArmor: ItemStack? = null

            for (armorItem in armorItems) {
                val itemStack = armorItem.entityItem
                val item = itemStack.item as ItemArmor

                if (item.armorType == armorType) {
                    if (bestArmor == null) {
                        if (itemStack > armor) {
                            bestEntityItem = armorItem
                            bestArmor = itemStack
                        }
                    } else {
                        if (itemStack > bestArmor) {
                            bestEntityItem = armorItem
                            bestArmor = itemStack
                        }
                    }
                }
            }

            scaledItems.add(bestEntityItem ?: continue)
        }
    }

    private fun scanInventory() {
        val gui = mc.currentScreen

        if (gui !is GuiInventory) return

        val container = gui.inventorySlots
        val slots = container.inventorySlots


        val inventory = mc.thePlayer.inventory
        val armorInventory = inventory.armorInventory


        for (armor in armorInventory) {
            armor ?: continue

            val armorType = (armor.item as ItemArmor).armorType

            var bestSlot: Slot? = null
            var bestArmor: ItemStack? = null

            for (slot in slots) {

                if (slot.inventory != inventory) continue

                if (slot.slotIndex in 36 until 40) continue

                val itemStack = slot.stack ?: continue
                val item = itemStack.item

                if (item is ItemArmor && item.armorType == armorType) {
                    if (bestArmor == null) {
                        if (itemStack > armor) {
                            bestArmor = itemStack
                            bestSlot = slot
                        }
                    } else {
                        if (itemStack > bestArmor) {
                            bestArmor = itemStack
                            bestSlot = slot
                        }
                    }
                }
            }

            drawSlots.add(bestSlot ?: continue)
        }
    }

    override fun onBackgroundDrawn(e: GuiScreenEvent.BackgroundDrawnEvent) {

        if (!enabled || Hypixel.currentGame != GameType.SKYWARS) return

        val gui = e.gui

        if (gui is GuiInventory) {
            for (slot in drawSlots) {
                var color = getParameterValue<Color>("color").rgb

                color = color and 0x50FFFFFF

                gui.drawOnSlot(slot, color)
            }
        }
    }

}