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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.base.SimpleFeature
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11

object Charm : SimpleFeature("charm", "Charm", "Allow you to look players through walls") {
    @JvmStatic
    val enabledJVM
        get() = enabled

    @JvmStatic
    fun render(partialTicks: Float) {
        clear(GL11.GL_DEPTH_BUFFER_BIT)

        disableEntityShadow {
            mc.entityRenderer.enableLightmap()
            blendFunc(770, 771)
            enableCull()
            bindTexture(0)
            color(-1f, -1f, -1f, -1f)

            for (player in mc.theWorld.playerEntities.filter {
                (it != mc.renderViewEntity || mc.gameSettings.thirdPersonView != 0 || it.isPlayerSleeping) &&
                        (it.posY !in 0.0..256.0 || mc.theWorld.isBlockLoaded(BlockPos(it)))
            }) {
                mc.renderManager.renderEntitySimple(player, partialTicks)
            }
            mc.entityRenderer.disableLightmap()
        }

        disableCull()
        color(1f, 1f, 1f, 0.5f)
    }
}