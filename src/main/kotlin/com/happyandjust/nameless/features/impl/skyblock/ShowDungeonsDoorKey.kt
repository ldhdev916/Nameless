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

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.stripControlCodes
import com.happyandjust.nameless.dsl.toVec3
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.color
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import java.awt.Color

object ShowDungeonsDoorKey : SimpleFeature(
    "dungeonsDoorKey",
    "Render Pointing Arrow to Blood/Wither Key",
    "Renders an arrow on your screen which is pointing to wither/blood key in hypixel skyblock dungeons. So you can find those keys easily"
) {

    private var keyPosition: Vec3? = null
    private val scanTimer = TickTimer.withSecond(0.5)

    init {
        parameter(Color.red.toChromaColor()) {
            matchKeyCategory()
            key = "color"
            title = "Direction Arrow Color"
        }
    }

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty(PropertyKey.DUNGEON)

    init {
        on<SpecialTickEvent>().filter { checkForRequirement() && scanTimer.update().check() }.subscribe {
            keyPosition = mc.theWorld.getEntitiesWithinAABB(
                EntityArmorStand::class.java,
                mc.thePlayer.entityBoundingBox.expand(16.0, 5.0, 16.0)
            )
                .find { it.displayName.unformattedText.stripControlCodes() in setOf("Wither Key", "Blood Key") }
                ?.toVec3()
        }

        on<SpecialOverlayEvent>().filter { checkForRequirement() }.subscribe {
            keyPosition?.let {
                RenderUtils.drawDirectionArrow(it, color.rgb)
            }
        }

        on<HypixelServerChangeEvent>().subscribe { keyPosition = null }
    }
}