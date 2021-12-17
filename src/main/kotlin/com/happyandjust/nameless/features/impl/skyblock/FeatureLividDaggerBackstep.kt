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
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import net.minecraft.entity.Entity
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

object FeatureLividDaggerBackstep : OverlayFeature(
    Category.SKYBLOCK,
    "lividdaggerbackstep",
    "Livid Dagger Backstep Notifier",
    "Draw HUD on screen when you hold livid dagger. Whether you'll backstep monster you're looking at or not. This could be inaccurate"
) {
    override var overlayPoint by ConfigValue("lividdagger", "overlay", Overlay.DEFAULT, COverlay)
    private var text: String? = null
    private val checkTimer = TickTimer.withSecond(0.25)

    override fun shouldDisplayInRelocateGui(): Boolean {
        return checkForRequirement()
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (!checkForRequirement()) return

        text?.let {
            matrix {
                setup(overlayPoint)
                mc.fontRendererObj.drawString(it, 0, 0, Color.green.rgb)
            }
        }
    }

    init {
        on<SpecialTickEvent>().filter { checkForRequirement() && checkTimer.update().check() }.subscribe {
            val objectMouseOver = mc.objectMouseOver ?: return@subscribe

            text = if (objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                if (detectBackstep(objectMouseOver.entityHit)) "Backstep!" else null
            } else null
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        return UIText("Backstep!").constrain {
            color = Color.green.constraint

            textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()
        }
    }

    private fun detectBackstep(lookingAt: Entity): Boolean {
        return lookingAt.horizontalFacing == mc.thePlayer.horizontalFacing
    }

    private fun checkForRequirement() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && mc.thePlayer.heldItem.getSkyBlockID() == "LIVID_DAGGER"
}