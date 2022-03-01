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

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.color
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.SkyBlock
import net.minecraft.entity.passive.EntityBat
import java.awt.Color

object GlowDungeonsBats : SimpleFeature("glowBats", "Glow Bats in SkyBlock Dungeons", "") {

    init {

        parameter(Color.pink.toChromaColor()) {
            matchKeyCategory()
            key = "color"
            title = "Glowing Color"
        }

        on<OutlineRenderEvent>().filter {
            val currentGame = Hypixel.currentGame
            enabled && currentGame is SkyBlock && currentGame.inDungeon && entity is EntityBat
        }.subscribe {
            colorInfo = ColorInfo(color.rgb, ColorInfo.ColorPriority.HIGHEST)
        }
    }
}