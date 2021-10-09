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

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.serialization.TypeRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityItemFrame
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityFishHook
import java.awt.Color
import kotlin.math.abs
import kotlin.math.pow

class FeatureGlowStarDungeonMobs : SimpleFeature(
    Category.SKYBLOCK,
    "stardungeonmobs",
    "Glow Star Dungeon Mobs",
    "Glow Star Dungeons mobs"
), ClientTickListener, StencilListener, ServerChangeListener {

    init {
        parameters["color"] = FeatureParameter(
            0,
            "stardungeonmobs",
            "color",
            "Outline Color",
            "",
            Color.yellow.toChromaColor(),
            TypeRegistry.getConverterByClass(ChromaColor::class)
        )
        parameters["fel"] = FeatureParameter(
            1,
            "stardungeonmobs",
            "fel",
            "Show Fel",
            "Make fel visible",
            false,
            TypeRegistry.getConverterByClass(Boolean::class)
        )
    }

    val checkedDungeonMobs = hashMapOf<EntityArmorStand, Entity>()
    private val ignoreMobs: (Entity) -> Boolean =
        { it is EntityArmorStand || it is EntityItem || it is EntityItemFrame || it is EntityXPOrb || it is EntityFishHook }
    private var checkTick = 0
    private var validTick = 0

    private fun checkForEnabledAndDungeon() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty(PropertyKey.DUNGEON)

    override fun tick() {
        if (!checkForEnabledAndDungeon()) return

        checkTick = (checkTick + 1) % 5
        validTick = (validTick + 1) % 50

        if (checkTick == 0) {
            for (entityArmorStand in mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
                .filter { !checkedDungeonMobs.contains(it) && it.displayName.unformattedText.contains("✯") }) {
                val availMobs = mc.theWorld.getEntitiesWithinAABB(
                    Entity::class.java,
                    entityArmorStand.entityBoundingBox.expand(0.6, 1.4, 0.6)
                ).also { it.removeIf(ignoreMobs) }
                    .sortedBy { (it.posX - entityArmorStand.posX).pow(2) + (it.posZ - entityArmorStand.posZ).pow(2) }

                if (availMobs.isEmpty()) { // this is cursed
                    continue
                }

                checkedDungeonMobs[entityArmorStand] = availMobs[0]
            }
        }

        if (validTick == 0) {
            val iterator = checkedDungeonMobs.iterator()
            for ((tag, mob) in iterator) {
                if (abs(tag.posX - mob.posX) >= 1 || abs(tag.posZ - mob.posZ) >= 1) {
                    iterator.remove()
                }
            }
        }
    }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        return if (checkedDungeonMobs.containsValue(entity)) {
            ColorInfo(getParameterValue<Color>("color").rgb, ColorInfo.ColorPriority.HIGHEST)
        } else null
    }

    override fun getEntityColor(entity: Entity): ColorInfo? = null

    override fun onServerChange(server: String) {
        checkedDungeonMobs.clear()
    }
}