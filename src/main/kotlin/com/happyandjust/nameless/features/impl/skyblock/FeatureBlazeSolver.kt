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
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.toSkyBlockMonster
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.hypixel.skyblock.DungeonFloor
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockMonster
import com.happyandjust.nameless.serialization.converters.CChromaColor
import net.minecraft.entity.Entity
import net.minecraft.entity.monster.EntityBlaze
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.awt.Color

object FeatureBlazeSolver : SimpleFeature(
    Category.SKYBLOCK,
    "blazesolver",
    "Blaze Solver",
    "Render outlines on the 'first' blaze and changes color of 'first' and 'second' blaze"
), ClientTickListener, StencilListener, ServerChangeListener {

    init {
        parameters["outlinecolor"] = FeatureParameter(
            0,
            "blazesolver",
            "outlinecolor",
            "Outline Color",
            "",
            Color.white.toChromaColor(),
            CChromaColor
        )

        parameters["firstcolor"] = FeatureParameter(
            1,
            "blazesolver",
            "firstcolor",
            "First Blaze's Color",
            "",
            Color(3, 86, 0).toChromaColor(),
            CChromaColor
        )

        parameters["secondcolor"] = FeatureParameter(
            2,
            "blazesolver",
            "secondcolor",
            "Second Blaze's Color",
            "",
            Color.green.toChromaColor(),
            CChromaColor
        )
    }

    private var blazeScanTick = 0
    private var currentBlazeRoomInfo: BlazeRoomInfo? = null

    override fun tick() {
        if (!enabled || Hypixel.currentGame != GameType.SKYBLOCK || !Hypixel.getProperty<Boolean>(PropertyKey.DUNGEON)) return
        if (Hypixel.getProperty<DungeonFloor>(PropertyKey.DUNGEON_FLOOR).floorInt < 2) return

        blazeScanTick = (blazeScanTick + 1) % 10

        if (blazeScanTick != 0) return

        val blazes = arrayListOf<SkyBlockMonster<EntityBlaze>>()

        for (blaze in mc.theWorld.loadedEntityList.filterIsInstance<EntityBlaze>()) {
            blazes.add(blaze.toSkyBlockMonster() ?: continue)
        }

        if (blazes.isEmpty()) {
            currentBlazeRoomInfo = null
            return
        }

        //suggest me a better detection
        val isLowest = currentBlazeRoomInfo?.isLowest ?: run {
            val chestLocation = scanChestLocation(blazes[0].actualEntity) ?: return

            chestLocation.y < blazes[0].actualEntity.posY
        }

        currentBlazeRoomInfo = BlazeRoomInfo(blazes, isLowest)
    }

    private fun scanChestLocation(checkEntity: EntityBlaze): BlockPos? {
        val from = BlockPos(checkEntity.posX - 7, checkEntity.posY - 40, checkEntity.posZ - 7)
        val to = BlockPos(checkEntity.posX + 7, checkEntity.posY + 40, checkEntity.posZ + 7)

        for (pos in BlockPos.getAllInBox(from, to)) {
            if (mc.theWorld.getBlockAtPos(pos) == Blocks.chest && mc.theWorld.getBlockAtPos(pos.up()) == Blocks.iron_bars) return pos
        }

        return null
    }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        return currentBlazeRoomInfo?.let {
            if (entity is EntityBlaze && it.sortByOrder()[0].actualEntity == entity) {
                ColorInfo(getParameterValue<Color>("outlinecolor").rgb, ColorInfo.ColorPriority.HIGHEST)
            } else null
        }
    }

    override fun getEntityColor(entity: Entity): ColorInfo? {
        return currentBlazeRoomInfo?.let {
            if (entity is EntityBlaze) {
                val sortByOrder = it.sortByOrder()

                if (entity == sortByOrder[0].actualEntity) {
                    ColorInfo(getParameterValue<Color>("firstcolor").rgb, ColorInfo.ColorPriority.HIGHEST)
                } else if (sortByOrder.size >= 2 && entity == sortByOrder[1].actualEntity) {
                    ColorInfo(getParameterValue<Color>("secondcolor").rgb, ColorInfo.ColorPriority.HIGHEST)
                } else null
            } else null
        }
    }

    data class BlazeRoomInfo(val blazes: List<SkyBlockMonster<EntityBlaze>>, val isLowest: Boolean) {

        fun sortByOrder() =
            if (isLowest) blazes.sortedBy { it.maxHealth } else blazes.sortedByDescending { it.maxHealth }
    }

    override fun onServerChange(server: String) {
        currentBlazeRoomInfo = null
    }

}