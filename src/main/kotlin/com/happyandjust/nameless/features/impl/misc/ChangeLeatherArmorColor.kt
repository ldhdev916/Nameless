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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import java.awt.Color

object ChangeLeatherArmorColor : SimpleFeature(
    "changeLeatherArmorColor",
    "Change Leather Armor Color",
    "Customize leather armor color"
) {

    init {
        parameter(true) {
            matchKeyCategory()
            key = "helmet"
            title = "Customize Helmet Color"

            parameter(Color.white.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Leather Helmet Color"
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "chestplate"
            title = "Customize Chestplate Color"

            settings {
                ordinal = 1
            }

            parameter(Color.white.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Leather Chestplate Color"
            }

        }

        parameter(true) {
            matchKeyCategory()
            key = "leggings"
            title = "Customize Leggings Color"

            settings {
                ordinal = 2
            }

            parameter(Color.white.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Leather Leggings Color"
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "boots"
            title = "Customize Boots Color"

            settings {
                ordinal = 3
            }

            parameter(Color.white.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Leather Boots Color"
            }
        }
    }

    @JvmStatic
    fun ItemArmor.getCustomColor(itemStack: ItemStack): Int? {
        if (!enabled || itemStack !in mc.thePlayer.inventory.armorInventory) return null
        return when (this) {
            Items.leather_helmet -> if (helmet) helmet_color else null
            Items.leather_chestplate -> if (chestplate) chestplate_color else null
            Items.leather_leggings -> if (leggings) leggings_color else null
            Items.leather_boots -> if (boots) boots_color else null
            else -> null
        }?.rgb
    }
}