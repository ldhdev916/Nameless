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

import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.stripControlCodes
import com.happyandjust.nameless.devqol.toVec3
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import java.awt.Color

object FeatureDungeonsDoorKey : SimpleFeature(
    Category.SKYBLOCK,
    "dungeonsdoorkey",
    "Render Pointing Arrow to Blood/Wither Key",
    "Renders an arrow on your screen which is pointing to wither/blood key in hypixel skyblock dungeons. So you can find those keys easily"
), RenderOverlayListener, ClientTickListener, ServerChangeListener {

    private var keyPosition: Vec3? = null
    private var scanTick = 0

    init {
        parameters["color"] = FeatureParameter(
            0,
            "dungeonsdoorkey",
            "color",
            "Direction Arrow Color",
            "",
            Color.red.toChromaColor(),
            CChromaColor
        )
    }

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty(PropertyKey.DUNGEON)


    override fun tick() {
        if (!checkForRequirement()) return

        scanTick = (scanTick + 1) % 10
        if (scanTick != 0) return

        for (entityArmorStand in mc.theWorld.getEntitiesWithinAABB(
            EntityArmorStand::class.java,
            mc.thePlayer.entityBoundingBox.expand(16.0, 5.0, 16.0)
        )) {
            val name = entityArmorStand.displayName.unformattedText.stripControlCodes()

            if (name == "Wither Key" || name == "Blood Key") {
                keyPosition = entityArmorStand.toVec3()
                return
            }
        }

        keyPosition = null
    }

    override fun renderOverlay(partialTicks: Float) {
        if (!checkForRequirement()) return

        keyPosition?.let {
            RenderUtils.drawDirectionArrow(it, getParameterValue<Color>("color").rgb)
        }
    }

    override fun onServerChange(server: String) {
        keyPosition = null
    }
}