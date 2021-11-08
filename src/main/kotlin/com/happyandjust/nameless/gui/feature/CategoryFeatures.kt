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

package com.happyandjust.nameless.gui.feature

import gg.essential.elementa.components.GradientComponent
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.invisible
import gg.essential.vigilance.gui.Divider
import gg.essential.vigilance.utils.onLeftClick

class CategoryFeatures(gui: FeatureGui, categoryItems: List<PropertyData<*>>) : UIContainer() {

    private val scroller by ScrollComponent(
        "No matching settings found :(",
        innerPadding = 4F,
        pixelsPerScroll = 25F,
        customScissorBoundingBox = this
    ).constrain {
        width = 100.percent() - 5.pixels()
        height = 100.percent() - 50.pixels()
    } childOf this

    private val scrollBar by UIBlock(ColorCache.scrollBar).constrain {
        x = SiblingConstraint() - 3.pixels()
        width = 3.pixels()
    } childOf this

    init {
        scroller.setVerticalScrollBarComponent(scrollBar, true)

        val sortByInCategory = hashMapOf<String, List<PropertyData<*>>>()

        for (propertyData in categoryItems.sortedWith(compareBy({ it.ordinal }, { it.title }))) {
            val existing = (sortByInCategory[propertyData.inCategory] ?: emptyList()).toMutableList()
            existing.add(propertyData)

            sortByInCategory[propertyData.inCategory] = existing
        }

        val sortedEntries = sortByInCategory.entries.sortedBy { it.key }

        for ((inCategory, propertyDatas) in sortedEntries) {
            if (inCategory.isNotBlank()) {
                Divider(inCategory, null) childOf scroller
            }
            for (propertyData in propertyDatas) {
                DataComponent(gui, propertyData) childOf scroller
            }
        }

        GradientComponent(ColorCache.background.invisible(), ColorCache.background).constrain {
            y = 0.pixel(alignOpposite = true)

            width = 100.percent() - 10.pixels()
            height = 50.pixels()
        }.onLeftClick {
            it.stopPropagation()
            scroller.mouseClick(it.absoluteX.toDouble(), it.absoluteY.toDouble(), it.mouseButton)
        }.onMouseScroll {
            it.stopPropagation()
            scroller.mouseScroll(it.delta)
        } childOf this

    }


}