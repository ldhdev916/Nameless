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

package com.happyandjust.nameless.gui.blatant

import com.happyandjust.nameless.features.BlatantFeature
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.GuiScale
import gg.essential.universal.USound
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class RequestUseBlatantFeatureGui(blatantFeature: BlatantFeature) : WindowScreen(
    drawDefaultBackground = false,
    restoreCurrentGuiOnClose = true,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal
) {

    private val background = UIRoundedRectangle(10f).constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        height = ChildBasedSizeConstraint() + 20.pixels()

        color = Color.black.withAlpha(0.7f).constraint
    } childOf window effect ScissorEffect()

    private val textContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint()
    } childOf background

    init {
        background.animate {
            setWidthAnimation(Animations.OUT_EXP, 1f, ChildBasedMaxSizeConstraint() + 20.pixels())
        }

        UIText("Warning!").constrain {
            x = CenterConstraint()

            textScale = 2.pixels()
            color = Color.red.constraint
        } childOf textContainer

        UIText("Feature §6${blatantFeature.title}§r is considered blatant for reason:").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)

        } childOf textContainer

        UIText(blatantFeature.reasonForBlatant).constrain {
            x = CenterConstraint()
            y = SiblingConstraint()

            color = Color.red.constraint
        } childOf textContainer

        UIText("Will you take a risk and accept using this feature?").constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)

            color = Color.green.constraint
        } childOf textContainer
    }

    private val buttonContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = SiblingConstraint(5f)

        width = ChildBasedSizeConstraint() + 20.pixels()
        height = ChildBasedMaxSizeConstraint()
    } childOf textContainer

    init {
        BlatantButton(Color.green.withAlpha(0.7f), "Accept") {
            mc.thePlayer.closeScreen()
            blatantFeature.useAccepted = true
        } childOf buttonContainer

        BlatantButton(Color.red.withAlpha(0.7f), "Deny") {
            mc.thePlayer.closeScreen()
        }.constrain {
            x = 0.pixel(true)
        } childOf buttonContainer
    }

    class BlatantButton(color: Color, text: String, action: () -> Unit) : UIRoundedRectangle(6f) {

        init {
            setColor(color)
            constrain {
                width = 40.pixels()
                height = 15.pixels()
            }

            UIText(text).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf this

            onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, color.withAlpha(0.5f).constraint)
                }
            }

            onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, color.constraint)
                }
            }

            onLeftClick {
                USound.playButtonPress()
                action()
            }
        }
    }


}