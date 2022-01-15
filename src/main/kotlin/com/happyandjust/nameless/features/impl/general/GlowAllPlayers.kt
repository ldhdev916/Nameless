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

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.mixins.accessors.AccessorEntity
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.Utils
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

object GlowAllPlayers : SimpleFeature(
    Category.GENERAL,
    "glowallplayers",
    "Glow All Players",
    "Glow all players in selected color except npc(hopefully)"
) {

    private var color by FeatureParameter(
        0,
        "glowplayers",
        "color",
        "Glowing Color",
        "",
        Color.red.toChromaColor(),
        CChromaColor
    )

    var invisible by FeatureParameter(
        0,
        "glowplayers",
        "invisible",
        "Show Invisible Players",
        "",
        false,
        CBoolean
    )

    @SubParameterOf("invisible")
    private var override by FeatureParameter(
        0,
        "glowplayers",
        "invisible_override",
        "Use Different Glowing Color on Invisible Players",
        "",
        false,
        CBoolean
    )

    @SubParameterOf("invisible")
    private var invisibleColor by FeatureParameter(
        1,
        "glowplayers",
        "invisible_color",
        "Color for Invisible Players",
        "Require 'Use Different Glowing Color on Invisible Players' to be enabled",
        Color.green.toChromaColor(),
        CChromaColor
    )

    var playersInTab = emptyList<EntityPlayer>()
    private val scanTimer = TickTimer(10)

    init {
        on<OutlineRenderEvent>().filter { enabled && entity in playersInTab }.subscribe {
            val color = if ((entity as AccessorEntity).invokeGetFlag(5) && override) {
                invisibleColor
            } else {
                color
            }.rgb

            colorInfo = ColorInfo(color, ColorInfo.ColorPriority.LOW)
        }

        on<SpecialTickEvent>().filter { scanTimer.update().check() }.subscribe {
            playersInTab = Utils.getPlayersInTab()
        }
    }

}