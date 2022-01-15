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
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import java.awt.Color

object ChangeLeatherArmorColor : SimpleFeature(
    Category.MISCELLANEOUS,
    "changeleatherarmorcolor",
    "Change Leather Armor Color",
    "Customize leather armor color"
) {

    private var helmet by FeatureParameter(
        0,
        "leatherarmorcolor",
        key,
        "Customize Helmet Color",
        "",
        true,
        CBoolean
    )

    private var chestplate by FeatureParameter(
        1,
        "leatherarmorcolor",
        key,
        "Customize Chestplate Color",
        "",
        true,
        CBoolean
    )

    private var leggings by FeatureParameter(
        2,
        "leatherarmorcolor",
        key,
        "Customize Leggings Color",
        "",
        true,
        CBoolean
    )

    private var boots by FeatureParameter(
        3,
        "leatherarmorcolor",
        key,
        "Customize Boots Color",
        "",
        true,
        CBoolean
    )

    @SubParameterOf("helmet")
    private var helmetColor by FeatureParameter(
        0,
        "leatherarmorcolor",
        "helmet_color",
        "Leather Helmet Color",
        "",
        Color.white.toChromaColor(),
        CChromaColor
    )

    @SubParameterOf("chestplate")
    private var chestplateColor by FeatureParameter(
        0,
        "leatherarmorcolor",
        "chestplate_color",
        "Leather Chestplate Color",
        "",
        Color.white.toChromaColor(),
        CChromaColor
    )

    @SubParameterOf("leggings")
    private var leggingsColor by FeatureParameter(
        0,
        "leatherarmorcolor",
        "leggings_color",
        "Leather Leggings Color",
        "",
        Color.white.toChromaColor(),
        CChromaColor
    )

    @SubParameterOf("boots")
    private var bootsColor by FeatureParameter(
        0,
        "leatherarmorcolor",
        "boots_color",
        "Leather Boots Color",
        "",
        Color.white.toChromaColor(),
        CChromaColor
    )

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