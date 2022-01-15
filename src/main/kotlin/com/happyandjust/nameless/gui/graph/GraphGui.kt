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

package com.happyandjust.nameless.gui.graph

import com.happyandjust.nameless.gui.ActionButton
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.GuiScale
import gg.essential.vigilance.utils.onLeftClick

class GraphGui : WindowScreen(
    ElementaVersion.V1,
    restoreCurrentGuiOnClose = true,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal
) {

    private val topContainer = UIContainer().constrain {
        width = 100.percent()
        height = 20.pixels()
    } childOf window

    init {

        UIText("y=").constrain {
            y = CenterConstraint()
            textScale = 2.pixels()
        } childOf topContainer


        val input = UITextInput("expression").constrain {

            x = SiblingConstraint()
            y = CenterConstraint()

            width = 80.percent()
            height = 100.percent()

            textScale = 2.pixels()
        } childOf topContainer

        input.onLeftClick {
            grabWindowFocus()
        }

        ActionButton("Update!") {
            graphComponent.updateExpression(input.getText())
        }.constrain {
            x = SiblingConstraint()
            y = CenterConstraint()

            width = FillConstraint(false)
            height = 90.percent()
        } childOf topContainer

    }

    private val graphComponent = GraphComponent(window, "").constrain {
        y = SiblingConstraint()

        width = 100.percent()
        height = FillConstraint(false)
    } childOf window
}