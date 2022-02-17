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

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.autoFillEnum
import com.happyandjust.nameless.features.base.listParameter
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.trajectory.*
import com.happyandjust.nameless.trajectory.TrajectoryPreview
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.item.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object TrajectoryPreview : SimpleFeature(
    "trajectoryPreview",
    "Trajectory Preview",
    "Shows trajectory preview of many projectiles"
) {

    init {
        parameter(false) {
            matchKeyCategory()
            key = "showTrace"
            title = "Enable Showing Trace of Trajectory"

            settings { subCategory = "Rendering" }

            parameter(Color.red.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Trace Color"
            }
        }

        parameter(Color.green.toChromaColor()) {
            matchKeyCategory()
            key = "endColor"
            title = "End Point Color"

            settings {
                ordinal = 1
                subCategory = "Rendering"
            }
        }

        parameter(Color.blue.toChromaColor()) {
            matchKeyCategory()
            key = "targetColor"
            title = "Target Point Color"
            desc = "Color when end point of trajectory HITS entity"

            settings {
                ordinal = 2
                subCategory = "Rendering"
            }
        }

        parameter(false) {

            matchKeyCategory()
            key = "glowTarget"
            title = "Glow Trajectory Target"
            desc = "Glow entity which is hit by end point of trajectory"

            settings {
                ordinal = 3
                subCategory = "Rendering"
            }

            parameter(Color(120, 5, 121).toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Glow Color"
            }
        }

        listParameter(listOf(TrajectoryType.BOW, TrajectoryType.ENDER_PEARL)) {
            matchKeyCategory()
            key = "selectedTypes"
            title = "Trajectory Types"

            settings {
                subCategory = "Type"
            }

            autoFillEnum { it.prettyName }
        }
    }

    private var trajectoryCalculateResult: TrajectoryCalculateResult? = null

    init {
        on<SpecialTickEvent>().filter { enabled }.subscribe {
            val preview = selectedTypes.find { it.enabled }?.trajectoryPreview ?: run {
                trajectoryCalculateResult = null
                return@subscribe
            }

            if (trajectoryCalculateResult == null) { // new
                preview.setRandomValue()
            }

            preview.init()
            trajectoryCalculateResult = preview.calculate()

        }

        on<RenderWorldLastEvent>().filter { enabled }.subscribe {
            trajectoryCalculateResult?.let {
                if (showTrace) {
                    RenderUtils.drawCurveLine(
                        it.renderTraces,
                        showTrace_color.rgb,
                        1.0,
                        partialTicks
                    )
                }

                it.end?.let { end ->
                    val pointColor =
                        (if (it.entityHit == null) endColor else targetColor).rgb

                    RenderUtils.draw3DPoint(end, pointColor, 4.0, partialTicks)
                }
            }
        }

        on<OutlineRenderEvent>().filter { enabled && glowTarget && entity == trajectoryCalculateResult?.entityHit }
            .subscribe {
                colorInfo = ColorInfo(glowTarget_color.rgb, ColorInfo.ColorPriority.HIGHEST)
            }
    }

    enum class TrajectoryType(val prettyName: String) {
        BOW("Bow") {
            override val enabled: Boolean
                get() = mc.thePlayer.itemInUse?.item is ItemBow
            override val trajectoryPreview: TrajectoryPreview
                get() = ArrowTrajectoryPreview()
        },
        EGG("Egg") {
            override val enabled: Boolean
                get() = mc.thePlayer.heldItem?.item is ItemEgg
            override val trajectoryPreview: TrajectoryPreview
                get() = ThrowableTrajectoryPreview()
        },
        ENDER_PEARL("Ender Pearl") {
            override val enabled: Boolean
                get() = mc.thePlayer.heldItem?.item is ItemEnderPearl
            override val trajectoryPreview: TrajectoryPreview
                get() = ThrowableTrajectoryPreview()
        },
        EXP_BOTTLE("Exp Bottle") {
            override val enabled: Boolean
                get() = mc.thePlayer.heldItem?.item is ItemExpBottle
            override val trajectoryPreview: TrajectoryPreview
                get() = ThrowableTrajectoryPreview(ThrowableTrajectoryPreview.ThrowableType.EXP_BOTTLE)
        },
        FISHING_ROD("Fishing Rod") {
            override val enabled: Boolean
                get() = mc.thePlayer.heldItem?.item is ItemFishingRod
            override val trajectoryPreview: TrajectoryPreview
                get() = FishHookTrajectory()
        },
        POTION("Potion") {
            override val enabled: Boolean
                get() = mc.thePlayer.heldItem?.let { it.item is ItemPotion && ItemPotion.isSplash(it.metadata) } == true
            override val trajectoryPreview: TrajectoryPreview
                get() = ThrowableTrajectoryPreview(ThrowableTrajectoryPreview.ThrowableType.POTION)
        },
        SNOWBALL("Snowball") {
            override val enabled: Boolean
                get() = mc.thePlayer.heldItem?.item is ItemSnowball
            override val trajectoryPreview: TrajectoryPreview
                get() = ThrowableTrajectoryPreview()
        };

        abstract val enabled: Boolean
        abstract val trajectoryPreview: TrajectoryPreview
    }
}