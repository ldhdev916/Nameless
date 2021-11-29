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

import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.mixins.accessors.AccessorEntity
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.Utils
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

object FeatureGlowAllPlayers : SimpleFeature(
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
            CChromaColor
        )

        parameters["invisible"] = FeatureParameter(
            0,
            "glowplayers",
            "invisible",
            "Show Invisible Players",
            "",
            false,
            CBoolean
        ).apply {
            parameters["override"] = FeatureParameter(
                0,
                "glowplayers",
                "invisible_override",
                "Use Different Glowing Color on Invisible Players",
                "",
                false,
                CBoolean
            )
            parameters["color"] = FeatureParameter(
                1,
                "glowplayers",
                "invisible_color",
                "Color for Invisible Players",
                "Require 'Use Different Glowing Color on Invisible Players' to be enabled",
                Color.green.toChromaColor(),
                CChromaColor
            )
        }
    }

    var playersInTab = emptyList<EntityPlayer>()
    private var scanTick = 0

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        if (!enabled) return null
        if (entity !is EntityPlayer) return null

        if (!playersInTab.contains(entity)) return null

        val parameter = getParameter<Boolean>("invisible")
        val color = if ((entity as AccessorEntity).invokeGetFlag(5) && parameter.getParameterValue("override")) {
            parameter.getParameterValue<Color>("color")
        } else {
            getParameterValue("color")
        }.rgb

        return ColorInfo(color, ColorInfo.ColorPriority.LOW)
    }

    override fun tick() {
        scanTick = (scanTick + 1) % 10
        if (scanTick == 0) {
            playersInTab = Utils.getPlayersInTab()
        }
    }

}