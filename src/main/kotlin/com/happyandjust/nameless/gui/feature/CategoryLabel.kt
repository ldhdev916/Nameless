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

package com.happyandjust.nameless.gui.feature

import com.happyandjust.nameless.features.Category
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.utils.onLeftClick

class CategoryLabel(private val gui: FeatureGui, val category: Category) : UIContainer() {
    private val text by UIText(category.displayName).constrain {
        y = CenterConstraint()

        textScale = 1.2.pixels()
        color = ColorCache.midText.constraint
    } childOf this

    var isSelected = false

    init {

        onLeftClick {
            if (!isSelected) {
                select()
            }
        }

        onMouseEnter {
            if (!isSelected) {
                text.animate {
                    setColorAnimation(Animations.OUT_EXP, .5F, ColorCache.accent.constraint)
                }
            }
        }

        onMouseLeave {
            if (!isSelected) {
                text.animate {
                    setColorAnimation(Animations.OUT_EXP, .5F, ColorCache.midText.constraint)
                }
            }
        }
    }

    fun select() {
        gui.selectCategory(category)
        isSelected = true

        text.animate {
            setColorAnimation(Animations.OUT_EXP, .5F, ColorCache.accent.constraint)
        }
    }

    fun deselect() {
        isSelected = false

        text.animate {
            setColorAnimation(Animations.OUT_EXP, .5F, ColorCache.midText.constraint)
        }
    }
}