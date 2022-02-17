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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.mixins.accessors.AccessorEntity
import com.happyandjust.nameless.utils.Utils
import net.minecraft.entity.player.EntityPlayer
import java.awt.Color

object GlowAllPlayers : SimpleFeature(
    "glowAllPlayers",
    "Glow All Players",
    "Glow all players in selected color except npc(hopefully)"
) {

    @JvmStatic
    var invisibleJVM
        get() = invisible
        set(value) {
            invisible = value
        }

    @JvmStatic
    val enabledJVM
        get() = enabled

    init {
        parameter(Color.red.toChromaColor()) {
            matchKeyCategory()
            key = "color"
            title = "Glowing Color"
        }

        parameter(false) {
            matchKeyCategory()
            key = "invisible"
            title = "Show Invisible Players"

            parameter(false) {
                matchKeyCategory()
                key = "override"
                title = "Use Different Glowing Color on Invisible Players"
            }

            parameter(Color.green.toChromaColor()) {
                matchKeyCategory()
                key = "color"

                settings {
                    ordinal = 1
                }

                title = "Color for Invisible Players"
                desc = "Require 'Use Different Glowing Color on Invisible Players' to be enabled"
            }
        }
    }

    @JvmField
    var playersInTab = emptyList<EntityPlayer>()
    private val scanTimer = TickTimer(10)

    init {
        on<OutlineRenderEvent>().filter { enabled && entity in playersInTab }.subscribe {
            val color = if ((entity as AccessorEntity).invokeGetFlag(5) && invisible_override) {
                invisible_color
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