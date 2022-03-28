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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.SkyBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityFishHook
import java.awt.Color
import kotlin.math.abs
import kotlin.math.pow

object GlowStarDungeonMobs : SimpleFeature(
    "starDungeonMobs",
    "Glow Star Dungeon Mobs",
    "Glow Star Dungeons mobs"
) {

    init {
        hierarchy {
            +::color

            +::showFel
        }
    }

    private var color by parameter(Color.yellow.toChromaColor()) {
        key = "color"
        title = "Outline Color"
    }

    @JvmStatic
    var showFel by parameter(false) {
        key = "showFel"
        title = "Show Fel"
        desc = "Make fel visible"

        settings { ordinal = 1 }
    }

    @JvmField
    val checkedDungeonMobs = hashMapOf<EntityArmorStand, Entity>()
    private val ignoreMobs: (Entity) -> Boolean =
        { it is EntityArmorStand || it is EntityItem || it is EntityItemFrame || it is EntityXPOrb || it is EntityFishHook || it == mc.thePlayer }
    private val checkTimer = TickTimer.withSecond(0.25)
    private val validTimer = TickTimer.withSecond(2.5)

    private fun checkForEnabledAndDungeon(): Boolean {
        val currentGame = Hypixel.currentGame
        return enabled && currentGame is SkyBlock && currentGame.inDungeon
    }

    init {
        on<SpecialTickEvent>().filter { checkForEnabledAndDungeon() }.subscribe {
            if (checkTimer.update().check()) {
                for (entityArmorStand in (mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>() - checkedDungeonMobs.keys).filter { "✯" in it.displayName.unformattedText }) {
                    val availMobs = mc.theWorld.getEntitiesWithinAABB(
                        Entity::class.java,
                        entityArmorStand.entityBoundingBox.expand(0.6, 1.4, 0.6)
                    )
                        .filter { !ignoreMobs(it) }
                        .sortedBy { (it.posX - entityArmorStand.posX).pow(2) + (it.posZ - entityArmorStand.posZ).pow(2) }

                    checkedDungeonMobs[entityArmorStand] = availMobs.firstOrNull() ?: continue
                }
            }
            if (validTimer.update().check()) {
                checkedDungeonMobs.entries.removeIf { abs(it.key.posX - it.value.posX) >= 1 || abs(it.key.posZ - it.value.posZ) >= 1 }
            }
        }

        on<OutlineRenderEvent>().filter { entity in checkedDungeonMobs.values }.subscribe {
            colorInfo = ColorInfo(color.rgb, ColorInfo.ColorPriority.HIGHEST)
        }

        on<HypixelServerChangeEvent>().subscribe { checkedDungeonMobs.clear() }
    }
}