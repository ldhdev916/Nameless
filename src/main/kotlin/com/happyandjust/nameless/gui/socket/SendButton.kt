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

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class SendButton(onClick: () -> Unit) : UIRoundedRectangle(6f) {

    init {
        onLeftClick { onClick() }
        setColor(Color.lightGray)

        onMouseEnter {
            animate {
                setColorAnimation(Animations.OUT_EXP, .5f, Color.lightGray.darker().constraint)
            }
        }

        onMouseLeave {
            animate {
                setColorAnimation(Animations.OUT_EXP, .5f, Color.lightGray.constraint)
            }
        }

        UIText("Send").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf this
    }
}