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
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
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
        hierarchy {
            ::helmet {
                +::helmetColor
            }

            ::chestplate {
                +::chestplateColor
            }

            ::leggings {
                +::leggingsColor
            }

            ::boots {
                +::bootsColor
            }
        }
    }

    private var helmet by parameter(true) {
        key = "helmet"
        title = "Customize Helmet Color"
    }

    private var helmetColor by parameter(Color.white.toChromaColor()) {
        key = "color"
        title = "Leather Helmet Color"
    }

    private var chestplate by parameter(true) {
        key = "chestplate"
        title = "Customize Chestplate Color"

        settings {
            ordinal = 1
        }
    }

    private var chestplateColor by parameter(Color.white.toChromaColor()) {
        key = "color"
        title = "Leather Chestplate Color"
    }

    private var leggings by parameter(true) {
        key = "leggings"
        title = "Customize Leggings Color"

        settings {
            ordinal = 2
        }
    }

    private var leggingsColor by parameter(Color.white.toChromaColor()) {
        key = "color"
        title = "Leather Leggings Color"
    }

    private var boots by parameter(true) {
        key = "boots"
        title = "Customize Boots Color"

        settings {
            ordinal = 3
        }
    }

    private var bootsColor by parameter(Color.white.toChromaColor()) {
        key = "color"
        title = "Leather Boots Color"
    }

    @JvmStatic
    fun ItemArmor.getCustomColor(itemStack: ItemStack): Int? {
        if (!enabled || itemStack !in mc.thePlayer.inventory.armorInventory) return null
        return when (this) {
            Items.leather_helmet -> if (helmet) helmetColor else null
            Items.leather_chestplate -> if (chestplate) chestplateColor else null
            Items.leather_leggings -> if (leggings) leggingsColor else null
            Items.leather_boots -> if (boots) bootsColor else null
            else -> null
        }?.rgb
    }
}