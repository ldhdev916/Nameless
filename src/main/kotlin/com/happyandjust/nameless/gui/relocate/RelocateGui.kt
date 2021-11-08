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

package com.happyandjust.nameless.gui.relocate

import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.core.Point
import com.happyandjust.nameless.features.IRelocateAble
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels

class RelocateGui(private val relocateAble: IRelocateAble) : WindowScreen(drawDefaultBackground = false) {
    private val relocateComponent = RelocateComponent(relocateAble.overlayPoint.value.scale).constrain {
        val point = relocateAble.overlayPoint.value.point

        x = point.x.pixels()
        y = point.y.pixels()
    } childOf window

    init {

        relocateComponent.onMouseScroll {
            val value = it.delta / relocateAble.getWheelSensitive()

            relocateComponent.changeScale(relocateComponent.currentScale + value)
        }

        relocateAble.getRelocateComponent(relocateComponent) childOf relocateComponent

        relocateComponent.fitIntoWindow(window)

        UIText("Drag element to change position, scroll to scale Up/Down").constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            textScale = 1.2.pixels()
            color = ColorCache.brightDivider.constraint
        } childOf window

        relocateComponent.changeScale(relocateAble.overlayPoint.value.scale)
    }

    override fun onScreenClose() {
        super.onScreenClose()

        relocateAble.overlayPoint.value = Overlay(
            Point(relocateComponent.getLeft().toInt(), relocateComponent.getTop().toInt()),
            relocateComponent.currentScale
        )
    }
}