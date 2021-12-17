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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityFishHook
import java.awt.Color
import kotlin.math.abs
import kotlin.math.pow

object FeatureGlowStarDungeonMobs : SimpleFeature(
    Category.SKYBLOCK,
    "stardungeonmobs",
    "Glow Star Dungeon Mobs",
    "Glow Star Dungeons mobs"
) {

    private var color by FeatureParameter(
        0,
        "stardungeonmobs",
        "color",
        "Outline Color",
        "",
        Color.yellow.toChromaColor(),
        CChromaColor
    )

    var showFel by FeatureParameter(
        1,
        "stardungeonmobs",
        "fel",
        "Show Fel",
        "Make fel visible",
        false,
        CBoolean
    )

    val checkedDungeonMobs = hashMapOf<EntityArmorStand, Entity>()
    private val ignoreMobs: (Entity) -> Boolean =
        { it is EntityArmorStand || it is EntityItem || it is EntityItemFrame || it is EntityXPOrb || it is EntityFishHook || it == mc.thePlayer }
    private val checkTimer = TickTimer.withSecond(0.25)
    private val validTimer = TickTimer.withSecond(2.5)

    private fun checkForEnabledAndDungeon() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty(PropertyKey.DUNGEON)

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