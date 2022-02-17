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

package com.happyandjust.nameless.gui.feature.components

import com.happyandjust.nameless.gui.feature.ColorCache
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.USound
import gg.essential.vigilance.gui.settings.SettingComponent
import gg.essential.vigilance.utils.onLeftClick

class MultiSelectorComponent(selected: List<String>, all: List<String>) : SettingComponent() {

    val dropDown by MultiDropDown(selected, all, this) childOf this

    init {
        constrain {
            y = 18.pixels()
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint()
        }
    }
}

class MultiDropDown(
    selected: List<String>,
    private val all: List<String>,
    private val parentComponent: MultiSelectorComponent
) : UIBlock(ColorCache.darkHighlight) {
    private val selectedIndex = selected.map { all.indexOf(it) }.toMutableList()
    private var expanded = false
    private val padding = 6f

    private val collapsedWidth by lazy {
        CopyConstraintFloat() boundTo topContainer
    }
    private val expandedWidth by lazy {
        (CopyConstraintFloat().to(scrollBoundingBox) + 16.pixels()) coerceAtLeast collapsedWidth
    }

    private val topContainer by UIContainer().constrain {
        width = ChildBasedSizeConstraint() + 16.pixels()
        height = 20.pixels()
    } childOf this

    private val selectText = UITextInput("Filter").constrain {
        x = 5.pixels()
        y = CenterConstraint()

        width = "A".repeat(8).width().pixels()
    } childOf topContainer

    init {
        selectText.onLeftClick {
            grabWindowFocus()
            if (expanded) it.stopPropagation()
        }.onKeyType { _, _ ->
            scroller.filterChildren {
                it !is CheckTextComponent || it.text.contains(selectText.getText(), true)
            }
        }
    }

    private val downArrow by UIImage.ofResourceCached(SettingComponent.DOWN_ARROW_PNG).constrain {
        x = 5.pixels(true)
        y = 7.5.pixels()
        width = 9.pixels()
        height = 5.pixels()
    } childOf topContainer

    private val upArrow by UIImage.ofResourceCached(SettingComponent.UP_ARROW_PNG).constrain {
        x = 5.pixels(true)
        y = 7.5.pixels()
        width = 9.pixels()
        height = 5.pixels()
    }

    private val scrollBoundingBox by UIContainer().constrain {
        x = 5.pixels()
        y = SiblingConstraint()

        width = ChildBasedMaxSizeConstraint()
    } childOf this

    private val scroller = ScrollComponent(customScissorBoundingBox = scrollBoundingBox) childOf scrollBoundingBox

    private val allComponents = all.mapIndexed { index, s ->
        CheckTextComponent(s, index) childOf scroller
    }

    init {
        constrain {
            width = collapsedWidth
            height = ChildBasedSizeConstraint()
        }
        scrollBoundingBox.hide()

        effect(OutlineEffect(ColorCache.divider, 1f))

        topContainer.onLeftClick {
            if (expanded) collapse() else expand()
            USound.playButtonPress()
        }
    }

    private fun expand() {
        expanded = true
        topContainer.replaceChild(upArrow, downArrow)

        setFloating(true)
        setWidth(expandedWidth)
        topContainer.setWidth(getWidth().pixels())

        scrollBoundingBox.unhide()
        scrollBoundingBox.animate {
            setHeightAnimation(Animations.IN_SIN, .35f, ChildBasedSizeConstraint() + padding.pixels())
        }
    }

    private fun collapse() {
        expanded = false
        topContainer.replaceChild(downArrow, upArrow)

        topContainer.setWidth(ChildBasedSizeConstraint() + 16.pixels())
        setWidth(collapsedWidth)

        scrollBoundingBox.animate {
            setHeightAnimation(Animations.OUT_SIN, .35f, 0.pixel())

            onComplete {
                scrollBoundingBox.hide()
                setFloating(false)
            }
        }
    }


    inner class CheckTextComponent(val text: String, index: Int) : UIContainer() {

        private val uiText by UIText(text).constrain {

            y = CenterConstraint()

            color = ColorCache.midText.constraint
        } childOf this

        private val checkImage by UIImage.ofResourceCached("/vigilance/check.png").constrain {
            x = SiblingConstraint(4f)
            y = CenterConstraint()

            width = 6.pixels()
            height = AspectConstraint()
        } childOf this

        init {

            if (index !in selectedIndex) checkImage.hide()

            constrain {
                y = SiblingConstraint(padding)

                width = ChildBasedSizeConstraint()
                height = ChildBasedMaxSizeConstraint()
            }

            onMouseEnter {
                uiText.animate {
                    setColorAnimation(Animations.OUT_EXP, .25f, ColorCache.brightText.constraint)
                }
            }
            onMouseLeave {
                uiText.animate {
                    setColorAnimation(Animations.OUT_EXP, .25f, ColorCache.midText.constraint)
                }
            }

            onMouseClick {
                it.stopPropagation()
                USound.playButtonPress()

                if (index in selectedIndex) {
                    checkImage.hide()
                    selectedIndex.remove(index)
                } else {
                    checkImage.unhide()
                    selectedIndex.add(index)
                }

                parentComponent.changeValue(selectedIndex.toList())
            }
        }
    }
}