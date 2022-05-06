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

package com.happyandjust.nameless.gui.socket

import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.UICircle
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class PlayerSelectIcon(playerName: String, private var selected: Boolean, onSelect: PlayerSelectIcon.() -> Unit) :
    UIContainer() {

    private fun getCircleColor() = if (selected) ColorCache.accent else Color.lightGray

    fun setSelected(selected: Boolean) {
        this.selected = selected
        outlineCircle.setColor(getCircleColor())
        innerCircle.setColor(getCircleColor())
    }

    init {

        PlayerIcon(playerName).constrain {
            y = CenterConstraint()

            width = 65.percent()
            height = AspectConstraint()
        } childOf this
    }


    private val circleContainer by UIContainer().constrain {
        x = SiblingConstraint(6f)
        y = CenterConstraint()

        width = FillConstraint(false)
        height = AspectConstraint()
    } childOf this

    init {
        circleContainer.onLeftClick {
            if (!selected) {
                onSelect()
            }
        }.onMouseEnter {
            if (!selected) {
                innerCircle.animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, getCircleColor().darker().constraint)
                }
            }
        }.onMouseLeave {
            if (!selected) {
                innerCircle.animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, getCircleColor().constraint)
                }
            }
        }
    }

    private val outlineCircle by UICircle(color = getCircleColor()).constrain {

        x = CenterConstraint()
        y = CenterConstraint()

        radius = 100.percent()

    } childOf circleContainer

    private val innerCircle by UICircle(color = getCircleColor()).constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        radius = 90.percent()
    } childOf circleContainer

    init {
        constrain {
            height = ChildBasedMaxSizeConstraint()
        }
    }
}