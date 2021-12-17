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

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.trajectory.ArrowTrajectoryPreview
import com.happyandjust.nameless.trajectory.FishHookTrajectory
import com.happyandjust.nameless.trajectory.ThrowableTrajectoryPreview
import com.happyandjust.nameless.trajectory.TrajectoryCalculateResult
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.init.Items
import net.minecraft.item.ItemPotion
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object FeatureTrajectoryPreview : SimpleFeature(
    Category.QOL,
    "trajectorypreview",
    "Trajectory Preview",
    "Shows trajectory preview of many projectiles"
) {

    @InCategory("Rendering")
    private var showTrace by FeatureParameter(
        0,
        "trajectory",
        "showtrace",
        "Enable Showing Trace of Trajectory",
        "",
        false,
        CBoolean
    )

    @SubParameterOf("showTrace")
    private var traceColor by FeatureParameter(
        0,
        "trajectory",
        "tracecolor",
        "Trace Color",
        "",
        Color.red.toChromaColor(),
        CChromaColor
    )

    @InCategory("Rendering")
    private var endColor by FeatureParameter(
        1,
        "trajectory",
        "endcolor",
        "End Point Color",
        "",
        Color.green.toChromaColor(),
        CChromaColor
    )

    @InCategory("Rendering")
    private var targetColor by FeatureParameter(
        2,
        "trajectory",
        "targetcolor",
        "Target Point Color",
        "Color when end point of trajectory HITS entity",
        Color.blue.toChromaColor(),
        CChromaColor
    )

    @InCategory("Rendering")
    private var glowTarget by FeatureParameter(
        3,
        "trajectory",
        "glowtarget",
        "Glow Trajectory Target",
        "Glow entity which is hit by end point of trajectory",
        false,
        CBoolean
    )

    @SubParameterOf("glowTarget")
    private var glowColor by FeatureParameter(
        0,
        "trajectory",
        "glowcolor",
        "Glow Color",
        "",
        Color(120, 5, 121).toChromaColor(),
        CChromaColor
    )

    @InCategory("Type")
    private var bow by FeatureParameter(
        0,
        "trajectory",
        "bow",
        "Enable Trajectory for Bow",
        "",
        true,
        CBoolean
    )

    @InCategory("Type")
    private var enderPearl by FeatureParameter(
        0,
        "trajectory",
        "enderpearl",
        "Enable Trajectory for Ender Pearl",
        "",
        true,
        CBoolean
    )

    @InCategory("Type")
    private var egg by FeatureParameter(
        0,
        "trajectory",
        "egg",
        "Enable Trajectory for Egg",
        "",
        true,
        CBoolean
    )

    @InCategory("Type")
    private var snowball by FeatureParameter(
        0,
        "trajectory",
        "snowball",
        "Enable Trajectory for Snowball",
        "",
        true,
        CBoolean
    )

    @InCategory("Type")
    private var potion by FeatureParameter(
        0,
        "trajectory",
        "potion",
        "Enable Trajectory for Potion",
        "",
        false,
        CBoolean
    )

    @InCategory("Type")
    private var expBottle by FeatureParameter(
        0,
        "trajectory",
        "expbottle",
        "Enable Trajectory for Exp Bottle",
        "",
        false,
        CBoolean
    )

    @InCategory("Type")
    private var fishingRod by FeatureParameter(
        0,
        "trajectory",
        "fishingrod",
        "Enable Trajectory for Fishing Rod",
        "",
        false,
        CBoolean
    )

    private var trajectoryCalculateResult: TrajectoryCalculateResult? = null

    init {
        on<SpecialTickEvent>().filter { enabled }.subscribe {
            val heldItemStack = mc.thePlayer.heldItem

            val heldItem = heldItemStack?.item ?: run {
                trajectoryCalculateResult = null
                return@subscribe
            }

            val preview = when (heldItem) {
                Items.snowball -> if (snowball) ThrowableTrajectoryPreview() else null
                Items.ender_pearl -> if (enderPearl) ThrowableTrajectoryPreview() else null
                Items.egg -> if (egg) ThrowableTrajectoryPreview() else null
                Items.bow -> if (mc.thePlayer.itemInUse?.item == Items.bow && bow) ArrowTrajectoryPreview() else null
                Items.potionitem -> if (potion && ItemPotion.isSplash(heldItemStack.metadata)) ThrowableTrajectoryPreview(
                    ThrowableTrajectoryPreview.ThrowableType.POTION
                ) else null
                Items.experience_bottle -> if (expBottle) ThrowableTrajectoryPreview(ThrowableTrajectoryPreview.ThrowableType.EXP_BOTTLE) else null
                Items.fishing_rod -> if (fishingRod) FishHookTrajectory() else null
                else -> null
            }

            if (trajectoryCalculateResult == null) { // new
                preview?.setRandomValue()
            }

            preview?.init()
            trajectoryCalculateResult = preview?.calculate()
        }

        on<RenderWorldLastEvent>().filter { enabled }.subscribe {
            trajectoryCalculateResult?.let {
                if (showTrace) {
                    RenderUtils.drawCurveLine(
                        it.renderTraces,
                        traceColor.rgb,
                        1.0,
                        partialTicks
                    )
                }

                it.end?.let { end ->
                    val pointColor =
                        (if (it.entityHit == null) endColor else traceColor).rgb

                    RenderUtils.draw3DPoint(end, pointColor, 4.0, partialTicks)
                }
            }
        }

        on<OutlineRenderEvent>().filter { enabled && glowTarget && entity == trajectoryCalculateResult?.entityHit }
            .subscribe {
                colorInfo = ColorInfo(glowColor.rgb, ColorInfo.ColorPriority.HIGHEST)
            }
    }
}