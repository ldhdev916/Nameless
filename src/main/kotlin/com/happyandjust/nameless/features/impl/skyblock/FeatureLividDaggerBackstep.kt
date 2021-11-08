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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.IRelocateAble
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels
import net.minecraft.entity.Entity
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

object FeatureLividDaggerBackstep : SimpleFeature(
    Category.SKYBLOCK,
    "lividdaggerbackstep",
    "Livid Dagger Backstep Notifier",
    "Draw HUD on screen when you hold livid dagger. Whether you'll backstep monster you're looking at or not. This could be inaccurate"
), ClientTickListener, IRelocateAble {
    override val overlayPoint = ConfigValue("lividdagger", "overlay", Overlay.DEFAULT, COverlay)
    private var text: String? = null
        get() = field.takeIf { checkForRequirement() }
    private var checkTick = 0

    override fun renderOverlay(partialTicks: Float) {
        if (!checkForRequirement()) return

        text?.let {
            matrix {
                val point = overlayPoint.value.point

                translate(point.x, point.y, 0)
                scale(overlayPoint.value.scale, overlayPoint.value.scale, 1.0)
                mc.fontRendererObj.drawString(it, 0, 0, Color.green.rgb)
            }
        }
    }

    override fun tick() {
        if (!checkForRequirement()) return
        val objectMouseOver = mc.objectMouseOver ?: return

        checkTick = (checkTick + 1) % 5

        if (checkTick != 0) return

        text = if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            if (detectBackstep(objectMouseOver.entityHit)) "Backstep!" else null
        } else null
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        return UIText("Backstep!").constrain {
            color = Color.green.constraint

            textScale = relocateComponent.currentScale.pixels()

            relocateComponent.onScaleChange {
                textScale = it.pixels()
            }
        }
    }

    private fun detectBackstep(lookingAt: Entity): Boolean {
        return lookingAt.horizontalFacing == mc.thePlayer.horizontalFacing
    }

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && mc.thePlayer.heldItem.getSkyBlockID() == "LIVID_DAGGER"
}