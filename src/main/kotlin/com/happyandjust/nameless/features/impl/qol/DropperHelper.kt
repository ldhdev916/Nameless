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

import com.happyandjust.nameless.core.input.InputPlaceHolder
import com.happyandjust.nameless.core.input.buildComposite
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.drawFilledBox
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.*
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.fixed
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.universal.UMatrixStack
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object DropperHelper : SimpleFeature(
    "dropperHelper",
    "Dropper Helper",
    "Render box on where you'll land"
) {

    init {
        hierarchy {
            +::boxColor

            showY {
                +::yText
            }
        }
    }

    private val textState = BasicState("")

    private var boxColor by parameter(Color.green.toChromaColor()) {
        key = "boxColor"
        title = "Box Color"
    }

    private var yText by userInputParameter(buildComposite {
        color { EnumChatFormatting.GREEN }
        value { "value" }
    }) {
        key = "yTextUser"
        title = "Y Text"

        settings {
            registeredPlaceHolders = listOf(InputPlaceHolder("value", "Y value"))
        }
    }

    private val showY = overlayParameter(false) {
        key = "showY"
        title = "Display Y Position"
        desc = "Render y position of where you'll land on your screen"

        config("dropper", "yPosition", Overlay.DEFAULT)

        component {
            UIText(yText.asString("value" to 999)).constrain {
                textScale = basicTextScaleConstraint { currentScale.toFloat() }.fixed()
            }
        }

        shouldDisplay { enabled && value }

        val window = Window(ElementaVersion.V1).apply {
            UIText().constrain {
                x = basicXConstraint { overlayPoint.x.toFloat() }.fixed()
                y = basicYConstraint { overlayPoint.y.toFloat() }.fixed()

                textScale = basicTextScaleConstraint { overlayPoint.scale.toFloat() }.fixed()
            }.bindText(textState) childOf this
        }

        render { if (enabled && value) window.draw(UMatrixStack.Compat.get()) }
    }

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
            textState.set(yText.asString("value" to aabb.minY.toInt()))
        }

        on<RenderWorldLastEvent>().filter { enabled }.subscribe {
            axisAlignedBB?.drawFilledBox(boxColor.rgb, partialTicks)
        }
    }
}