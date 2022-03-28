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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.overlayParameter
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.gui.OverlayConstraint.Companion.constraint
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.PartyGames
import com.happyandjust.nameless.hypixel.partygames.LabEscape
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMatrixStack
import java.awt.Color

object PartyGamesHelper : SimpleFeature("partyGamesHelper", "Party Games Helper") {

    init {
        hierarchy {
            +::jigsaw

            ::rpg16 {
                +::rpg16Color
            }

            ::avalanche {
                +::avalancheColor
            }

            ::animal {
                +::animalColor
            }

            ::anvil {
                +::anvilColor
            }

            +::maze

            ::dive {
                +::diveColor
            }

            +::labEscape

            ::highGround {
                +::highGroundColor
            }
        }
    }

    var jigsaw by parameter(true) {
        key = "jigsaw"
        title = "Jigsaw Rush"
        desc = "Render which key to press on your canvas"
    }

    var rpg16 by parameter(true) {
        key = "rpg16"
        title = "RPG 16"
        desc = "Glow player who is at 1 heart"
    }

    var rpg16Color by parameter(Color.red.toChromaColor()) {
        key = "color"
        title = "Player Color"
    }

    var avalanche by parameter(true) {
        key = "avalanche"
        title = "Avalanche"
        desc = "Render box under the slabs"
    }

    var avalancheColor by parameter(Color.magenta.toChromaColor()) {
        key = "color"
        title = "Box Color"
    }

    var animal by parameter(true) {
        key = "animal"
        title = "Animal Slaughter"
        desc = "Glow -50% entity so you don't hit them"
    }

    var animalColor by parameter(Color.red.toChromaColor()) {
        key = "color"
        title = "-50% Entity COlor"
    }

    var anvil by parameter(true) {
        key = "anvil"
        title = "Anvil Spleef"
        desc = "Render box on where the anvils will land on"
    }

    var anvilColor by parameter(Color.red.withAlpha(0.4f).toChromaColor()) {
        key = "color"
        title = "Box Color"
    }

    var maze by parameter(true) {
        key = "maze"
        title = "Spizer Maze"
        desc = "Nothing to explain, maze solver"
    }

    var dive by parameter(true) {
        key = "dive"
        title = "Dive"
        desc =
            "Render 'Exact Box' on where you'll land on, color will turn into red if you'll collide with block when you land"
    }

    var diveColor by parameter(Color.blue.toChromaColor()) {
        key = "color"
        title = "Box Color"
    }

    var labEscape by overlayParameter(true) {
        key = "labEscape"
        title = "Lab Escape"
        desc = "Render what key you have to press on your screen"

        config("partyGamesHelper", "labEscapeOverlay", Overlay.DEFAULT)

        component {
            val container = UIContainer().constrain {
                width = ChildBasedMaxSizeConstraint()
                height = ChildBasedSizeConstraint()
            }

            for (text in arrayOf("1", "1", "2", "3", "2")) {
                UIText(text).constrain {
                    y = SiblingConstraint()

                    textScale = basicTextScaleConstraint { currentScale.toFloat() }.fixed()

                    color = Color.red.constraint
                } childOf container
            }

            container
        }

        shouldDisplay { enabled && value && Hypixel.currentGame is PartyGames }

        val window = Window(ElementaVersion.V1)
        val container = UIContainer().constrain {
            x = overlayPoint.constraint()
            y = overlayPoint.constraint()

            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        } childOf window
        val textComponents = List(5) {
            UIText().constrain {
                y = SiblingConstraint()

                textScale = overlayPoint.constraint()
            } childOf container
        }
        render {
            withInstance<PartyGames>(Hypixel.currentGame) {
                withInstance<LabEscape>(partyMiniGames) {
                    keys.forEachIndexed { index, key ->
                        textComponents[index].setText(key)
                    }

                    window.draw(UMatrixStack.Compat.get())
                }
            }
        }
    }

    var highGround by parameter(true) {
        key = "highGround"
        title = "High Ground"
        desc = "Glow players whose score is higher than you(only if player is in scoreboard)"
    }

    var highGroundColor by parameter(Color.red.toChromaColor()) {
        key = "color"
        title = "Glowing Color"
    }
}