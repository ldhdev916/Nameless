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

import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.dsl.inCategory
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.trajectory.ArrowTrajectoryPreview
import com.happyandjust.nameless.trajectory.FishHookTrajectory
import com.happyandjust.nameless.trajectory.ThrowableTrajectoryPreview
import com.happyandjust.nameless.trajectory.TrajectoryCalculateResult
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.item.ItemPotion
import java.awt.Color

object FeatureTrajectoryPreview : SimpleFeature(
    Category.QOL,
    "trajectorypreview",
    "Trajectory Preview",
    "Shows trajectory preview of many projectiles"
), ClientTickListener, WorldRenderListener, StencilListener {

    init {
        inCategory(
            "Rendering",
            "showtrace" to FeatureParameter(
                0,
                "trajectory",
                "showtrace",
                "Enable Showing Trace of Trajectory",
                "",
                false,
                CBoolean
            ).apply {
                parameters["tracecolor"] = FeatureParameter(
                    0,
                    "trajectory",
                    "tracecolor",
                    "Trace Color",
                    "",
                    Color.red.toChromaColor(),
                    CChromaColor
                )
            },
            "endcolor" to FeatureParameter(
                1,
                "trajectory",
                "endcolor",
                "End Point Color",
                "",
                Color.green.toChromaColor(),
                CChromaColor
            ),
            "targetcolor" to FeatureParameter(
                2,
                "trajectory",
                "targetcolor",
                "Target Point Color",
                "Color when end point of trajectory HITS entity",
                Color.blue.toChromaColor(),
                CChromaColor
            ),
            "glowtarget" to FeatureParameter(
                3,
                "trajectory",
                "glowtarget",
                "Glow Trajectory Target",
                "Glow entity which is hit by end point of trajectory",
                false,
                CBoolean
            ).apply {
                parameters["glowcolor"] = FeatureParameter(
                    0,
                    "trajectory",
                    "glowcolor",
                    "Glow Color",
                    "",
                    Color(120, 5, 121).toChromaColor(),
                    CChromaColor
                )
            },
        )

        inCategory(
            "Type",
            "bow" to FeatureParameter(
                0,
                "trajectory",
                "bow",
                "Enable Trajectory for Bow",
                "",
                true,
                CBoolean
            ),
            "enderpearl" to FeatureParameter(
                0,
                "trajectory",
                "enderpearl",
                "Enable Trajectory for Ender Pearl",
                "",
                true,
                CBoolean
            ),
            "egg" to FeatureParameter(
                0,
                "trajectory",
                "egg",
                "Enable Trajectory for Egg",
                "",
                true,
                CBoolean
            ),
            "snowball" to FeatureParameter(
                0,
                "trajectory",
                "snowball",
                "Enable Trajectory for Snowball",
                "",
                true,
                CBoolean
            ),
            "potion" to FeatureParameter(
                0,
                "trajectory",
                "potion",
                "Enable Trajectory for Potion",
                "",
                false,
                CBoolean
            ),
            "expbottle" to FeatureParameter(
                0,
                "trajectory",
                "expbottle",
                "Enable Trajectory for Exp Bottle",
                "",
                false,
                CBoolean
            ),
            "fishingrod" to FeatureParameter(
                0,
                "trajectory",
                "fishingrod",
                "Enable Trajectory for Fishing Rod",
                "",
                false,
                CBoolean
            )
        )

    }

    private var trajectoryCalculateResult: TrajectoryCalculateResult? = null

    override fun tick() {
        if (!enabled) return

        val heldItemStack = mc.thePlayer.heldItem

        val heldItem = heldItemStack?.item ?: run {
            trajectoryCalculateResult = null
            return
        }

        val preview = when (heldItem) {
            Items.snowball -> if (getParameterValue("snowball")) ThrowableTrajectoryPreview() else null
            Items.ender_pearl -> if (getParameterValue("enderpearl")) ThrowableTrajectoryPreview() else null
            Items.egg -> if (getParameterValue("egg")) ThrowableTrajectoryPreview() else null
            Items.bow -> if (mc.thePlayer.itemInUse?.item == Items.bow && getParameterValue("bow")) ArrowTrajectoryPreview() else null
            Items.potionitem -> if (getParameterValue("potion") && ItemPotion.isSplash(heldItemStack.metadata)) ThrowableTrajectoryPreview(
                ThrowableTrajectoryPreview.ThrowableType.POTION
            ) else null
            Items.experience_bottle -> if (getParameterValue("potion")) ThrowableTrajectoryPreview(
                ThrowableTrajectoryPreview.ThrowableType.EXP_BOTTLE
            ) else null
            Items.fishing_rod -> if (getParameterValue("fishingrod")) FishHookTrajectory() else null
            else -> null
        }

        if (trajectoryCalculateResult == null) { // new
            preview?.setRandomValue()
        }

        preview?.init()
        trajectoryCalculateResult = preview?.calculate()

    }

    override fun renderWorld(partialTicks: Float) {
        if (!enabled) return
        trajectoryCalculateResult?.let {

            if (getParameterValue("showtrace")) {
                RenderUtils.drawCurveLine(
                    it.renderTraces,
                    getParameter<Boolean>("showtrace").getParameterValue<Color>("tracecolor").rgb,
                    1.0,
                    partialTicks
                )
            }

            it.end?.let { end ->
                val pointColor =
                    (if (it.entityHit == null) getParameterValue<Color>("endcolor") else getParameterValue("targetcolor")).rgb

                RenderUtils.draw3DPoint(end, pointColor, 4.0, partialTicks)
            }
        }
    }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        if (!enabled) return null
        if (!getParameterValue<Boolean>("glowtarget")) return null
        if (trajectoryCalculateResult?.entityHit == entity) return ColorInfo(
            getParameter<Boolean>("glowtarget").getParameterValue<Color>("glowcolor").rgb,
            ColorInfo.ColorPriority.HIGHEST
        )

        return null
    }

}