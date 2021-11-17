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

import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.dsl.getSkyBlockRarity
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.Item

object FeatureGlowDroppedItem : SimpleFeature(
    Category.SKYBLOCK,
    "glowdroppeditem",
    "Glow Dropped Item",
    "Glow dropped item on skyblock according to its rarity"
), ClientTickListener, StencilListener {

    private var itemRarityCache = hashMapOf<EntityItem, ColorInfo>()
    private var scanTick = 0

    private fun checkForRequirement() = enabled && Hypixel.currentGame == GameType.SKYBLOCK

    override fun tick() {
        if (!checkForRequirement()) return

        scanTick = (scanTick + 1) % 20

        if (scanTick == 0) {
            val map = hashMapOf<EntityItem, ColorInfo>()
            for (entityItem in mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>()
                .filter { !isShopShowcaseItem(it) }) {
                map[entityItem] = ColorInfo(
                    entityItem.entityItem.getSkyBlockRarity()?.color ?: continue,
                    ColorInfo.ColorPriority.HIGHEST
                )
            }

            itemRarityCache = map
        }
    }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        if (!checkForRequirement()) return null
        if (entity !is EntityItem) return null

        return itemRarityCache[entity]
    }

    override fun getEntityColor(entity: Entity): ColorInfo? = null

    private fun isShopShowcaseItem(entityItem: EntityItem): Boolean {
        val list = entityItem.worldObj.getEntitiesWithinAABB(EntityArmorStand::class.java, entityItem.entityBoundingBox)
        if (list.isEmpty()) return false

        val flag: (EntityArmorStand) -> Boolean = {
            !it.isInvisible || it.getEquipmentInSlot(4)?.item != Item.getItemFromBlock(Blocks.glass)
        }
        return list.any { !flag(it) }
    }
}