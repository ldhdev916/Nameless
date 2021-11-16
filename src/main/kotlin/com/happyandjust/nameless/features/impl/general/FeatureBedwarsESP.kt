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

import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.utils.Utils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor

object FeatureBedwarsESP : SimpleFeature(
    Category.GENERAL,
    "bedwarsesp",
    "Bedwars ESP",
    "Glow all players in your game according to their team color."
), ClientTickListener, StencilListener, ServerChangeListener {

    private fun checkForEnabledAndBedwars() = enabled && Hypixel.currentGame == GameType.BEDWARS
    val teamColorCache = hashMapOf<EntityPlayer, Int>()
    private var scanTick = 0

    init {
        parameters["invisible"] = FeatureParameter(
            0,
            "bedwarsesp",
            "invisible",
            "Glow Invisible Players",
            "If you turn this on, invisibility on players will be removed",
            true,
            CBoolean
        )
    }

    override fun tick() {
        if (!checkForEnabledAndBedwars()) return

        // You know once assigned team color doesn't change, so we don't need to scan every tick
        // Known bugs: NPC go brrrr
        scanTick = (scanTick + 1) % 40

        if (scanTick == 0) {
            for (player in Utils.getPlayersInTab().filter { !teamColorCache.containsKey(it) }) {
                val chestplate = player.getEquipmentInSlot(3) ?: continue

                val item = chestplate.item

                if (item !is ItemArmor || item.armorMaterial != ItemArmor.ArmorMaterial.LEATHER) continue

                val color = item.getColor(chestplate) or 0xFF000000.toInt()

                teamColorCache[player] = color
            }
        }
    }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        if (!checkForEnabledAndBedwars()) return null
        if (entity !is EntityPlayer) return null

        val color = teamColorCache[entity] ?: return null

        return ColorInfo(color, ColorInfo.ColorPriority.HIGH)
    }

    override fun getEntityColor(entity: Entity): ColorInfo? = null

    override fun onServerChange(server: String) {
        teamColorCache.clear()
    }

}