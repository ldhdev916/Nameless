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
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.features.base.BaseFeature
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.GuiScale
import gg.essential.universal.UKeyboard
import gg.essential.universal.USound
import gg.essential.vigilance.utils.onLeftClick
import java.util.*

class FeatureGui : WindowScreen(
    ElementaVersion.V1,
    drawDefaultBackground = false,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal,
    restoreCurrentGuiOnClose = true
) {
    init {
        UIBlock(ColorCache.background).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf window
    }

    private val content by UIContainer().constrain {
        x = CenterConstraint()
        y = CenterConstraint()
        // width = 85.percent()
        height = 75.percent()
    } childOf window

    override fun initScreen(width: Int, height: Int) {
        super.initScreen(width, height)

        content.animate {
            setWidthAnimation(Animations.OUT_EXP, 1f, 85.percent())
        }
    }

    private val backContainer by UIContainer().constrain {
        x = SiblingConstraint(20f, alignOpposite = true) boundTo content
        y = 0.5.pixels() boundTo content

        width = ChildBasedSizeConstraint() + 20.pixels()
        height = ChildBasedSizeConstraint() + 20.pixels()
    } childOf window

    private val backIcon by UIText("<").constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        color = ColorCache.divider.constraint

        textScale = 1.35.pixels()
    } childOf backContainer

    init {

        backIcon.hide()

        backContainer.onMouseEnter {
            backIcon.animate {
                setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.accent.constraint)
            }
        }.onMouseLeave {
            backIcon.animate {
                setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.divider.constraint)
            }
        }.onLeftClick {
            USound.playButtonPress()

            if (componentStack.isNotEmpty()) {
                componentStack.pop().hide()
            }

            if (componentStack.isNotEmpty()) {
                componentStack.peek().unhide()
            }

            updateBackIconState()
        }
    }

    private val featureTitleBar by FeatureTitleBar(this, window).constrain {
        width = 100.percent()
        height = 30.pixels()
    } childOf content

    private val mainContent by UIContainer().constrain {
        y = SiblingConstraint()
        width = FillConstraint(false)
        height = FillConstraint(false)
    } childOf content

    private val categoryScrollContainer by UIContainer().constrain {
        width = 25.percent()
        height = 100.percent()
    } childOf mainContent

    private val categoryScroller by ScrollComponent(customScissorBoundingBox = categoryScrollContainer).constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf categoryScrollContainer

    init {

        UIBlock(ColorCache.divider).constrain {

            y = (-.5F).pixels()
            x = SiblingConstraint()

            width = 1.pixel()
            height = 100.percent() + .5F.pixels()
        } childOf mainContent

        for (category in Category.values()) {
            CategoryLabel(this, category).constrain {
                x = 15.pixels()
                y = SiblingConstraint()

                width = ChildBasedSizeConstraint()
                height = ChildBasedSizeConstraint() + 14.pixels()
            } childOf categoryScroller
        }
    }

    private val mainSettingsContainer by UIContainer().constrain {
        x = SiblingConstraint() + 5.pixels()

        width = FillConstraint(false)
        height = 100.percent()
    } childOf mainContent

    init {
        UIBlock(ColorCache.divider).constrain {
            width = 1.pixel()
            height = 100.percent()
        } childOf content

        UIBlock(ColorCache.divider).constrain {
            x = 0.pixel(alignOpposite = true)
            width = 1.pixel
            height = 100.percent()
        } childOf content

        window.onKeyType { _, keyCode ->
            if (keyCode == UKeyboard.KEY_MINUS) {

                window.childrenOfType<Inspector>().forEach {
                    window.removeChild(it)
                }

                Inspector(window) childOf window
            }
        }
    }

    fun selectCategory(category: Category) {
        selectCategory(FeatureRegistry.featuresByCategory[category]!!)
    }

    @JvmName("selectCategoryByFeatureList")
    fun selectCategory(list: List<BaseFeature<*>>, resetText: Boolean = true) {
        hidePeek()

        componentStack.clear()

        val categoryItems = list.map { it.toPropertyData() }

        deselect()

        addComponentToMainSettings(CategoryFeatures(this, categoryItems, true).constrain {
            width = 100.percent()
            height = 100.percent()
        }, resetText)
    }

    fun deselect() {
        categoryScroller.allChildren.filterIsInstance<CategoryLabel>().firstOrNull { it.isSelected }?.deselect()
    }

    private val componentStack = Stack<UIComponent>()

    fun filterPropertyData(text: String) {

        if (componentStack.isEmpty()) {
            selectCategory(FeatureRegistry.features, false)
        }

        val peek = componentStack.peek()

        if (peek is CategoryFeatures) {
            peek.filter(text)
        }

    }

    fun addComponentToMainSettings(component: UIComponent, resetText: Boolean = true) {

        if (resetText) featureTitleBar.searchTextInput.setText("")

        hidePeek()

        component childOf mainSettingsContainer

        componentStack.push(component)

        updateBackIconState()
    }

    private fun hidePeek() {
        if (componentStack.isNotEmpty()) componentStack.peek().hide()
    }

    private fun updateBackIconState() {
        if (componentStack.size <= 1) {
            backIcon.hide()
        } else {
            backIcon.unhide()
        }
    }
}