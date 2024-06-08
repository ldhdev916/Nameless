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

package com.happyandjust.nameless.gui.relocate

import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.features.base.IRelocateAble
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*

class RelocateGui(relocateAbleList: List<IRelocateAble>) :
    WindowScreen(ElementaVersion.V5, drawDefaultBackground = false) {

    private val map = relocateAbleList.associateWith { RelocateComponent(this, it).childOf(window) }
    val yCenterLine = UIBlock(ColorCache.accent).constrain {

        y = CenterConstraint()

        width = 100.percent()
        height = 0.5.pixel()
    } childOf window
    val xCenterLine = UIBlock(ColorCache.accent).constrain {
        x = CenterConstraint()

        width = 0.5.pixel()
        height = 100.percent()
    } childOf window
    val centerX
        get() = window.getWidth() / 2
    val centerY
        get() = window.getHeight() / 2

    init {
        xCenterLine.hide()
        yCenterLine.hide()

        UIText("Drag element to change position, scroll to scale Up/Down").constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            textScale = 1.2.pixels()
            color = ColorCache.brightDivider.constraint
        } childOf window
    }

    override fun onScreenClose() {
        super.onScreenClose()

        for ((relocateAble, component) in map) {
            relocateAble.overlayPoint = Overlay(
                component.getLeft().toInt(),
                component.getTop().toInt(),
                component.currentScale
            )
        }
    }
}