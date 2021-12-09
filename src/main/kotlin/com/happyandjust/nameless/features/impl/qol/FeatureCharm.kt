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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.dsl.clear
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.WorldRenderListener
import org.lwjgl.opengl.GL11

object FeatureCharm : SimpleFeature(Category.QOL, "charm", "Charm", "Allow you to look players through walls"),
    WorldRenderListener {
    override fun renderWorld(partialTicks: Float) {
        if (enabled) {
            clear(GL11.GL_DEPTH_BUFFER_BIT)
            for (player in mc.theWorld.playerEntities) {
                if (player != mc.renderViewEntity && !player.canEntityBeSeen(mc.renderViewEntity)) {
                    mc.renderManager.renderEntitySimple(player, partialTicks)
                }
            }
        }
    }
}