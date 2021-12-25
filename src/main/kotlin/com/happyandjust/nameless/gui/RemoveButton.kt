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

package com.happyandjust.nameless.gui

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class RemoveButton(action: () -> Unit) : UIContainer() {

    private val image = UIImage.ofResource("/nameless/xmark.png").constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        width = 80.percent()
        height = AspectConstraint()

        color = Color.red.withAlpha(0.7f).constraint
    } childOf this

    init {

        constrain {
            width = 20.pixels()
            height = 20.pixels()
        }

        effect(OutlineEffect(Color.red, 1f))

        onLeftClick { action() }

        onMouseEnter {
            image.animate {
                setColorAnimation(Animations.OUT_EXP, .5f, Color.red.constraint)
            }
        }

        onMouseLeave {
            image.animate {
                setColorAnimation(Animations.OUT_EXP, .5f, Color.red.withAlpha(0.7f).constraint)
            }
        }
    }
}