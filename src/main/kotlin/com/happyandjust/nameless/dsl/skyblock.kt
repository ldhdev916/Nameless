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

package com.happyandjust.nameless.dsl

import com.happyandjust.nameless.core.FAIRY_SOUL
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.Constants
import java.util.regex.Pattern

private val RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)")

/**
 * Taken from SkyblockAddons under MIT License
 *
 * Modified
 *
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author Biscuit
 */
fun ItemStack?.getSkyBlockRarity(): ItemRarity? {
    this ?: return null
    if (!hasTagCompound()) return null

    val display = getSubCompound("display", false) ?: return null
    if (!display.hasKey("Lore")) return null
    val lore = display.getTagList("Lore", Constants.NBT.TAG_STRING)
    val values = ItemRarity.values()

    return List(lore.tagCount()) {
        RARITY_PATTERN.findMatcher(lore.getStringTagAt(it)) {
            values.find { rarity -> group("rarity").startsWith(rarity.loreName) }
        }
    }.filterNotNull().firstOrNull()
}

fun ItemStack?.getSkyBlockID(): String {
    this ?: return ""
    val tagCompound = getSubCompound("ExtraAttributes", false) ?: return ""
    return tagCompound.getString("id")
}


fun EntityArmorStand.isFairySoul(): Boolean {
    if (Hypixel.currentGame != GameType.SKYBLOCK) return false
    return getEquipmentInSlot(4)?.getSkullOwner()?.getMD5() == FAIRY_SOUL
}