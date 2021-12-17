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
import com.happyandjust.nameless.dsl.getSkyBlockRarity
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.Item

object FeatureGlowDroppedItem : SimpleFeature(
    Category.SKYBLOCK,
    "glowdroppeditem",
    "Glow Dropped Item",
    "Glow dropped item on skyblock according to its rarity"
) {

    private val itemRarityCache = hashMapOf<EntityItem, ColorInfo>()
    private val scanTimer = TickTimer.withSecond(1)

    private fun checkForRequirement() = enabled && Hypixel.currentGame == GameType.SKYBLOCK

    init {
        on<SpecialTickEvent>().filter { checkForRequirement() && scanTimer.update().check() }.subscribe {
            itemRarityCache.clear()
            for (entityItem in mc.theWorld.loadedEntityList.filterIsInstance<EntityItem>()
                .filterNot { isShopShowcaseItem(it) }) {
                itemRarityCache[entityItem] = ColorInfo(
                    entityItem.entityItem.getSkyBlockRarity()?.color ?: continue,
                    ColorInfo.ColorPriority.HIGHEST
                )
            }
        }

        on<OutlineRenderEvent>().filter { checkForRequirement() && entity is EntityItem }
            .subscribe {
                colorInfo = itemRarityCache[entity]
            }
    }


    private fun isShopShowcaseItem(entityItem: EntityItem): Boolean {
        val list = entityItem.worldObj.getEntitiesWithinAABB(EntityArmorStand::class.java, entityItem.entityBoundingBox)
        if (list.isEmpty()) return false

        val flag: (EntityArmorStand) -> Boolean = {
            !it.isInvisible || it.getEquipmentInSlot(4)?.item != Item.getItemFromBlock(Blocks.glass)
        }
        return list.any { !flag(it) }
    }
}