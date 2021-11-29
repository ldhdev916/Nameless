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

package com.happyandjust.nameless.gui.feature.components

import com.google.gson.JsonElement
import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.utils.invisible
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color

class VerticalPositionEditableComponent(
    allIdentifiers: List<Identifier>,
    addedIdentifiers: List<Identifier>
) : SettingComponent() {

    private val padding = 5f
    private val notAddedContainer by UIContainer().constrain {
        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint(padding)
    } childOf this effect OutlineEffect(Color.black, 3f)

    private val addedContainer by UIContainer().constrain {

        x = 0.pixel(true)

        width = ChildBasedMaxSizeConstraint()
        height = ChildBasedSizeConstraint(padding)
    } childOf this effect OutlineEffect(Color.black, 3f)

    private val componentIdentifierMap = hashMapOf<MoveAbleComponent, Identifier>()

    init {
        constrain {
            width = ChildBasedSizeConstraint() + 40.pixels()
            height = ChildBasedMaxSizeConstraint()
        }

        for ((text, container) in arrayOf("Available" to notAddedContainer, "Current" to addedContainer)) {
            UIText(text).constrain {
                x = CenterConstraint()

                color = Color(255, 128, 0).constraint
                textScale = 2.pixels()
            } childOf container
        }

        val filterAddedIdentifiers =
            addedIdentifiers.filter { allIdentifiers.any { identifier -> it.areEqual(identifier) } }


        allIdentifiers.filter { filterAddedIdentifiers.none { identifier -> it.areEqual(identifier) } }.forEach {
            val component = MoveAbleComponent(it.toUIComponent(this)).constrain {
                y = SiblingConstraint(padding)
            } childOf notAddedContainer

            componentIdentifierMap[component] = it
        }

        filterAddedIdentifiers.forEach {
            val component = MoveAbleComponent(it.toUIComponent(this)).constrain {
                y = SiblingConstraint(padding)
            } childOf addedContainer

            componentIdentifierMap[component] = it
        }
    }

    private fun convertToPixels(requireSibling: Boolean = true) {
        val children = (notAddedContainer.children + addedContainer.children).filterIsInstance<MoveAbleComponent>()

        if (!requireSibling || children.any { it.constraints.y is SiblingConstraint }) {
            for (child in children) {
                child.constrain {
                    y = (child.getTop() - child.parent.getTop()).pixels()
                }
            }
        }
    }

    private fun convertToSiblings() {
        for (child in (notAddedContainer.children + addedContainer.children).filterIsInstance<MoveAbleComponent>()) {
            child.constrain {
                y = SiblingConstraint(padding)
            }
        }
    }

    fun saveValue() {
        changeValue(getValue())
    }

    fun getValue() = addedContainer.children.filterIsInstance<MoveAbleComponent>().sortedBy { it.getTop() }
        .map { componentIdentifierMap[it]!! }


    inner class MoveAbleComponent(child: UIComponent) : UIBlock(ColorCache.brightDivider.invisible()) {

        private val container by UIContainer().constrain {
            width = AspectConstraint()
            height = basicHeightConstraint { child.getHeight() }
        } childOf this

        private val threeLinesBlock by UIImage.ofResource("/nameless/threelines.png").constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            width = 70.percent()
            height = ImageAspectConstraint()
        } childOf container

        private var offset: Pair<Float, Float>? = null

        private var lastY = 0f

        init {
            constrain {
                width = ChildBasedSizeConstraint()
                height = ChildBasedMaxSizeConstraint()
            }

            child childOf this

            child.constrain {
                x = SiblingConstraint()
                y = CenterConstraint()
            }

            onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.brightDivider.withAlpha(0.5f).constraint)
                }
            }

            onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.brightDivider.invisible().constraint)
                }
            }

            onLeftClick {
                convertToPixels()
                offset = it.relativeX to it.relativeY
                lastY = getTop()
            }

            onMouseRelease {
                if (offset != null) {

                    constrain {
                        x = 0.pixel()
                        y = basicYConstraint { lastY }
                    }
                    offset = null

                    convertToPixels(false)
                }
            }

            onMouseDrag { mouseX, mouseY, mouseButton ->
                if (constraints.y !is SiblingConstraint) {
                    offset?.let {
                        if (mouseButton == 0) {
                            constrain {
                                val moveX = mouseX + getLeft() - it.first
                                val moveY = mouseY + getTop() - it.second

                                x = basicXConstraint { moveX }
                                y = basicYConstraint { moveY }

                                if (!recalculateParent(moveX)) {
                                    if (parent == addedContainer) {
                                        recalculatePosition(moveY)
                                    }
                                }
                                saveValue()
                            }
                        }
                    }
                }
            }
        }

        private fun recalculateParent(x: Float): Boolean {
            val compareParent = if (parent == notAddedContainer) addedContainer else notAddedContainer

            return if (x in compareParent.getLeft()..compareParent.getRight()) {
                Window.enqueueRenderOperation {
                    hide(true)
                    this childOf compareParent

                    convertToSiblings()
                    Window.enqueueRenderOperation {
                        lastY = getTop()
                        convertToPixels()
                    }
                }
                true
            } else false
        }

        private fun recalculatePosition(y: Float) {
            val positionMap = hashMapOf<MoveAbleComponent, Float>()

            for (child in parent.children.filterIsInstance<MoveAbleComponent>()) {
                positionMap[child] = if (child === this) lastY else child.getTop()
            }

            val positions = positionMap.values.sorted()
            val componentArray = positionMap.keys.sortedBy { if (it === this) y else positionMap[it]!! }

            for ((index, component) in componentArray.withIndex()) {
                val positionY = positions[index]

                if (component === this) {
                    lastY = positionY

                    continue
                }

                component.constrain {
                    this.y = basicYConstraint { positionY }
                }
            }
        }
    }
}

interface Identifier {
    fun toUIComponent(gui: VerticalPositionEditableComponent): UIComponent

    fun serialize(): JsonElement

    fun areEqual(other: Identifier): Boolean
}