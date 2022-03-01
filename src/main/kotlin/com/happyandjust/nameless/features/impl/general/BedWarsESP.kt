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
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.withAlpha
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.invisible
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.BedWars
import com.happyandjust.nameless.utils.Utils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemArmor

object BedWarsESP : SimpleFeature(
    "bedWarsEsp",
    "BedWars ESP",
    "Glow all players in your game according to their team color."
) {

    @JvmStatic
    val enabledJVM
        get() = enabled

    @JvmStatic
    var invisibleJVM
        get() = invisible
        set(value) {
            invisible = value
        }

    private fun checkForEnabledAndBedwars() = enabled && Hypixel.currentGame is BedWars

    @JvmField
    val teamColorCache = hashMapOf<EntityPlayer, Int>()
    private val scanTimer = TickTimer.withSecond(2)

    init {
        parameter(true) {
            matchKeyCategory()
            key = "invisible"
            title = "Glow Invisible Players"
            desc = "If you turn this on, invisibility on players will be removed"
        }

        on<SpecialTickEvent>().filter { checkForEnabledAndBedwars() && scanTimer.update().check() }.subscribe {
            for (player in Utils.getPlayersInTab() - teamColorCache.keys) {
                val chestplate = player.getEquipmentInSlot(3) ?: continue

                val item = chestplate.item

                if (item !is ItemArmor || item.armorMaterial != ItemArmor.ArmorMaterial.LEATHER) continue
                teamColorCache[player] = item.getColor(chestplate).withAlpha(1f)
            }
        }

        on<OutlineRenderEvent>().filter { checkForEnabledAndBedwars() }.subscribe {
            val cachedColor = teamColorCache[entity]
            if (cachedColor != null) {
                colorInfo = ColorInfo(cachedColor, ColorInfo.ColorPriority.HIGH)
            }
        }

        on<HypixelServerChangeEvent>().subscribe { teamColorCache.clear() }
    }

}