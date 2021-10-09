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

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.serialization.TypeRegistry
import com.happyandjust.nameless.utils.Utils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

class FeatureGlowAllPlayers : SimpleFeature(
    Category.GENERAL,
    "glowallplayers",
    "Glow All Players",
    "Glow all players in selected color except npc(hopefully)"
), StencilListener, ClientTickListener {

    init {
        parameters["color"] = FeatureParameter(
            0,
            "glowplayers",
            "color",
            "Glowing Color",
            "",
            Color.red.toChromaColor(),
            TypeRegistry.getConverterByClass(ChromaColor::class)
        )
    }

    private var playersInTab = emptyList<EntityPlayer>()
    private var scanTick = 0


    override fun getOutlineColor(entity: Entity): ColorInfo? {
        if (!enabled) return null
        if (entity !is EntityPlayer) return null

        if (!playersInTab.contains(entity)) return null

        return ColorInfo(getParameterValue<Color>("color").rgb, ColorInfo.ColorPriority.LOW)
    }

    override fun getEntityColor(entity: Entity): ColorInfo? = null

    override fun tick() {
        scanTick = (scanTick + 1) % 10

        if (scanTick == 0) {
            playersInTab = Utils.getPlayersInTab()
        }
    }

}