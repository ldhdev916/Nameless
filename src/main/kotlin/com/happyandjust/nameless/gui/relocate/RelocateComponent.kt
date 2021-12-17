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

import com.happyandjust.nameless.features.IRelocateAble
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.invisible
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.math.abs

class RelocateComponent(gui: RelocateGui, relocateAble: IRelocateAble) : UIBlock(ColorCache.brightDivider.invisible()) {

    private var offset: Pair<Float, Float>? = null
    var currentScale = relocateAble.overlayPoint.scale

    private val indicatorText = UIText(relocateAble.getDisplayName()).constrain {

        y = 0.pixel(alignOutside = true) boundTo this@RelocateComponent
        textScale = 1.5.pixels()

        color = Color.red.invisible().constraint
    } childOf gui.window

    init {
        constrain {

            val point = relocateAble.overlayPoint.point

            x = point.x.pixels()
            y = point.y.pixels()

            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        fun UIBlock.isPresent() = isChildOf(gui.window)

        onMouseEnter {
            animate {
                setColorAnimation(Animations.OUT_EXP, 1f, ColorCache.brightDivider.withAlpha(0.7f).constraint)
            }
            indicatorText.constrain {
                x = CenterConstraint() boundTo this@RelocateComponent
            }
            indicatorText.animate {
                setColorAnimation(Animations.OUT_EXP, 1f, Color.red.constraint)
            }
        }

        onMouseLeave {
            animate {
                setColorAnimation(Animations.OUT_EXP, 1f, ColorCache.brightDivider.invisible().constraint)
            }

            indicatorText.animate {
                setColorAnimation(Animations.OUT_EXP, 1f, Color.red.invisible().constraint)
            }
        }

        onLeftClick {
            offset = it.relativeX to it.relativeY
        }

        onMouseRelease {
            offset = null

            if (gui.xCenterLine.isPresent()) {
                Window.enqueueRenderOperation { gui.xCenterLine.hide(true) }
            }
            if (gui.yCenterLine.isPresent()) {
                Window.enqueueRenderOperation { gui.yCenterLine.hide(true) }
            }
        }

        onMouseDrag { mouseX, mouseY, mouseButton ->
            offset?.let {
                if (mouseButton == 0) {
                    constrain {
                        x = (mouseX + getLeft() - it.first).pixels()
                        y = (mouseY + getTop() - it.second).pixels()
                    }

                    if (abs(getLeft() + getWidth() / 2 - gui.centerX) <= 1) {
                        Window.enqueueRenderOperation { gui.xCenterLine.unhide(true) }
                        setX((gui.centerX - getWidth() / 2).pixels())

                    } else if (gui.xCenterLine.isPresent()) {
                        Window.enqueueRenderOperation { gui.xCenterLine.hide(true) }
                    }
                    if (abs(getTop() + getHeight() / 2 - gui.centerY) <= 1) {
                        Window.enqueueRenderOperation { gui.yCenterLine.unhide(true) }
                        setY((gui.centerY - getHeight() / 2).pixels())

                    } else if (gui.yCenterLine.isPresent()) {
                        Window.enqueueRenderOperation { gui.yCenterLine.hide(true) }
                    }

                    fitIntoWindow(gui.window)
                }
            }
        }

        onMouseScroll {
            val value = it.delta / relocateAble.getWheelSensitive()

            changeScale(currentScale + value)
        }

        relocateAble.getRelocateComponent(this) childOf this
        changeScale(relocateAble.overlayPoint.scale)
        fitIntoWindow(gui.window)
    }

    private fun changeScale(newValue: Double) {
        currentScale = newValue.coerceAtLeast(0.05)
    }

    private fun fitIntoWindow(window: Window) {

        val right = window.getRight() - getWidth()

        val bottom = window.getBottom() - getHeight()

        constrain {
            if (right > 0) {
                x = x.coerceIn(0.pixel(), right.pixels())
            }

            if (bottom > 0) {
                y = y.coerceIn(0.pixel(), bottom.pixels())
            }
        }
    }


}