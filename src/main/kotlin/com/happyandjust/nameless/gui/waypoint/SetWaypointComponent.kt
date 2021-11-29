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

package com.happyandjust.nameless.gui.waypoint

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.gui.feature.ColorCache
import com.happyandjust.nameless.gui.feature.components.toChromaColorComponent
import com.happyandjust.nameless.listener.WaypointListener
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.gui.settings.CheckboxComponent
import gg.essential.vigilance.gui.settings.ColorComponent
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.BlockPos
import java.awt.Color

abstract class WaypointComponent : UIContainer() {
    init {
        constrain {
            x = CenterConstraint()
            y = SiblingConstraint(10f)

            width = ChildBasedSizeConstraint() + 10.pixels()
            height = ChildBasedMaxSizeConstraint() + 10.pixels()
        }
    }
}

class DummyWaypointComponent : WaypointComponent() {
    init {
        setupTextComponents(
            UIText("Waypoint Name"),
            UIText("Target Position"),
            UIText("Can Fly"),
            UIText("Enabled"),
            UIText("Color")
        )
    }
}

class SetWaypointComponent(
    private val parentGui: WaypointManagerGui,
    private val waypointInfo: WaypointListener.WaypointInfo
) :
    WaypointComponent() {

    init {
        setupTextComponents(
            UITextInput("Name").apply {
                setText(waypointInfo.name)

                onFocusLost {
                    waypointInfo.name = getText()
                }
                onLeftClick {
                    grabWindowFocus()
                }
            }.constrain {
                width = 90.pixels()
            },
            with(waypointInfo.targetPos) {
                UITextInput("Position(split with comma)").apply {
                    setText("$x, $y, $z")

                    onFocusLost {
                        val split = getText().filter { it.isDigit() || it == ',' }.trim(',').split(",")
                        if (split.size == 3) {
                            waypointInfo.targetPos = BlockPos(split[0].toInt(), split[1].toInt(), split[2].toInt())
                        }
                        with(waypointInfo.targetPos) {
                            setText("$x, $y, $z")
                        }
                    }
                    onLeftClick {
                        grabWindowFocus()
                    }
                }.constrain {
                    width = 90.pixels()
                }
            },
            CheckboxComponent(waypointInfo.canFly).apply {
                onValueChange {
                    waypointInfo.canFly = it as Boolean
                }
            },
            CheckboxComponent(waypointInfo.enabled).apply {
                onValueChange {
                    waypointInfo.enabled = it as Boolean
                }
            },
            ColorComponent(waypointInfo.color, false).toChromaColorComponent(waypointInfo.color.chromaEnabled).apply {
                onValueChange {
                    waypointInfo.color = it as ChromaColor
                }
            }
        )
        enableEffect(OutlineEffect(ColorCache.accent, 1f))
    }

    inner class RemoveComponent : UIContainer() {
        init {

            constrain {
                width = 20.pixels()
                height = 20.pixels()
            }

            effect(OutlineEffect(Color.red, 1f))

            onLeftClick {
                WaypointListener.waypointInfos.remove(waypointInfo)
                parentGui.scroller.removeChild(this@SetWaypointComponent)
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


private fun WaypointComponent.setupTextComponents(
    nameComponent: UIComponent,
    positionComponent: UIComponent,
    canFlyComponent: UIComponent,
    enabledComponent: UIComponent,
    colorComponent: UIComponent,
) {

    val container = UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        width = ChildBasedSizeConstraint()
        height = ChildBasedMaxSizeConstraint()
    } childOf this


    nameComponent.constrain {
        y = CenterConstraint()
    } childOf container

    positionComponent.constrain {
        x = SiblingConstraint(50f)
        y = CenterConstraint()
    } childOf container

    canFlyComponent.constrain {
        x = SiblingConstraint(50f)
        y = CenterConstraint()
    } childOf container

    enabledComponent.constrain {
        x = SiblingConstraint(50f)
        y = CenterConstraint()
    } childOf container

    colorComponent.constrain {
        x = SiblingConstraint(50f)
        y = CenterConstraint()
    } childOf container

    if (this is SetWaypointComponent) {
        RemoveComponent().constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf container
    }
}