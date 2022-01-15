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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.serialization.converters.COverlay
import com.happyandjust.nameless.serialization.converters.CString
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.universal.UMatrixStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object DropperHelper : SimpleFeature(
    Category.QOL,
    "dropperhelper",
    "Dropper Helper",
    "Render box on where you'll land"
) {

    private val textState = BasicState("")
    private var boxColor by FeatureParameter(
        0,
        "dropper",
        "color",
        "Box Color",
        "Box color when NORMAL state",
        Color.green.toChromaColor(),
        CChromaColor
    )

    private var showY by object : OverlayParameter<Boolean>(
        1,
        "dropper",
        "showy",
        "Display Y Position",
        "Render y position of where you'll land on your screen",
        false,
        CBoolean
    ) {
        override var overlayPoint by ConfigValue("dropper", "yposition", Overlay.DEFAULT, COverlay)
        private val window = Window(ElementaVersion.V1)
        private val matrixStack by lazy { UMatrixStack.Compat.get() }

        init {
            UIText().constrain {
                x = basicXConstraint { overlayPoint.point.x.toFloat() }.fixed()
                y = basicYConstraint { overlayPoint.point.y.toFloat() }.fixed()

                textScale = basicTextScaleConstraint { overlayPoint.scale.toFloat() }.fixed()
            }.bindText(textState) childOf window
        }

        override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
            return UIText(getYText(99)).constrain {
                textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()
            }
        }

        override fun shouldDisplayInRelocateGui() = enabled && value

        override fun renderOverlay0(partialTicks: Float) {
            if (enabled && value) {
                window.draw(matrixStack)
            }
        }
    }

    @SubParameterOf("showY")
    private var yText by FeatureParameter(0, "dropper", "ytext", "Y Text", "", "&a{value}", CString)

    private fun getYText(y: Int) = yText.replace("&", "§").replace("{value}", y.toString())

    private var axisAlignedBB: AxisAlignedBB? = null

    init {
        on<SpecialTickEvent>().filter { enabled }.subscribe {
            var aabb = mc.thePlayer.entityBoundingBox.run {
                val pos = BlockPos(mc.thePlayer)
                AxisAlignedBB(minX, pos.y.toDouble(), minZ, maxX, pos.y.toDouble() + 1.0, maxZ)
            }

            while (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, aabb).isEmpty()) {
                aabb = aabb.offset(0.0, -1.0, 0.0)
            }
            axisAlignedBB = aabb
            textState.set(getYText(aabb.minY.toInt()))
        }

        on<RenderWorldLastEvent>().filter { enabled }.subscribe {
            axisAlignedBB?.let {
                RenderUtils.drawBox(it, boxColor.rgb, partialTicks)
            }
        }
    }
}