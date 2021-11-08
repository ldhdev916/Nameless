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

import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.invisible
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.utils.onLeftClick

class RelocateComponent(var currentScale: Double) : UIBlock(ColorCache.brightDivider.invisible()) {

    private var offset: Pair<Float, Float>? = null
    private val scaleChangeListeners = arrayListOf<(Double) -> Unit>()

    init {
        constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        onMouseEnter {
            animate {
                setColorAnimation(Animations.OUT_EXP, 1f, ColorCache.brightDivider.withAlpha(0.7f).constraint)
            }
        }

        onMouseLeave {
            animate {
                setColorAnimation(Animations.OUT_EXP, 1f, ColorCache.brightDivider.invisible().constraint)
            }
        }

        onLeftClick {
            offset = it.relativeX to it.relativeY
        }

        onMouseRelease {
            offset = null
        }

        onMouseDrag { mouseX, mouseY, mouseButton ->
            offset?.let {
                if (mouseButton == 0) {
                    constrain {
                        x = (mouseX + getLeft() - it.first).pixels()
                        y = (mouseY + getTop() - it.second).pixels()
                    }

                    fitIntoWindow(Window.of(this))
                }
            }
        }
    }

    fun changeScale(newValue: Double) {
        currentScale = newValue.coerceAtLeast(0.05)

        scaleChangeListeners.forEach { it(currentScale) }
    }

    fun onScaleChange(listener: (Double) -> Unit) = apply {
        scaleChangeListeners.add(listener)
    }

    fun fitIntoWindow(window: Window) {

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