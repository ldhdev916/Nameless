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

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.serialization.converters.CChromaColor
import net.minecraft.entity.passive.EntityBat
import java.awt.Color

object FeatureGlowBats : SimpleFeature(Category.SKYBLOCK, "glowbats", "Glow Bats in SkyBlock Dungeons", "") {

    private var color by FeatureParameter(
        0,
        "glowbats",
        "color",
        "Glowing Color",
        "",
        Color.pink.toChromaColor(),
        CChromaColor
    )

    init {
        on<OutlineRenderEvent>().filter {
            enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty(
                PropertyKey.DUNGEON
            ) && entity is EntityBat
        }.subscribe {
            colorInfo = ColorInfo(color.rgb, ColorInfo.ColorPriority.HIGHEST)
        }
    }
}