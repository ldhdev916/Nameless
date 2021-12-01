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

package com.happyandjust.nameless.gui.shortcmd

import com.happyandjust.nameless.commands.ShortCommand
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class CommandInfoComponent(
    private val parentGui: ShortCommandGui,
    private val shortCommandInfo: ShortCommand.ShortCommandInfo
) : UIContainer() {

    init {

        constrain {
            y = SiblingConstraint(10f)

            width = 100.percent() - 10.pixels()
            height = ChildBasedMaxSizeConstraint() + 10.pixels()
        }

        effect(OutlineEffect(ColorCache.accent, 1f))

        val inputWidth = 50.percent() - 60.pixels()

        UITextInput("Shortened").apply {
            setText(shortCommandInfo.short)

            onFocusLost {
                shortCommandInfo.short = getText()
            }
        }.constrain {
            y = CenterConstraint()

            width = inputWidth
        }.onLeftClick {
            grabWindowFocus()
        } childOf this

        UITextInput("Origin").apply {
            setText(shortCommandInfo.origin)

            onFocusLost {
                shortCommandInfo.origin = getText()
            }
        }.constrain {
            x = 30.pixel(true)
            y = CenterConstraint()

            width = inputWidth

        }.onLeftClick {
            grabWindowFocus()
        } childOf this

        RemoveComponent().constrain {
            x = 10.pixel(true)
            y = CenterConstraint()
        } childOf this
    }

    inner class RemoveComponent : UIContainer() {

        init {
            constrain {
                width = 20.pixels()
                height = 20.pixels()
            }

            effect(OutlineEffect(Color.red, 1f))

            onLeftClick {
                parentGui.list.remove(shortCommandInfo)
                parentGui.scroller.removeChild(this@CommandInfoComponent)
            }

            onMouseEnter {
                text.animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, Color.red.constraint)
                }
            }

            onMouseLeave {
                text.animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, Color.red.withAlpha(0.7f).constraint)
                }
            }
        }

        private val text = UIText("X").constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            textScale = 1.3.pixels()

            color = Color.red.withAlpha(0.7f).constraint
        } childOf this
    }
}