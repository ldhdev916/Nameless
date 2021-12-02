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

import com.happyandjust.nameless.gui.feature.components.Identifier
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.Setting
import gg.essential.vigilance.gui.settings.ButtonComponent
import kotlin.reflect.KMutableProperty0

data class PropertyData<T>(
    val property: KMutableProperty0<T>,
    val title: String,
    val desc: String,
    val componentType: ComponentType?,
) {

    var ordinal = 0

    var settings: List<PropertyData<*>> = emptyList()
    var inCategory = ""

    var placeHolder: String? = null

    var validator: (Char) -> Boolean = { true }

    var minValue: Double = 0.0
    var maxValue: Double = 0.0

    var allEnumList = emptyList<Enum<*>>()
    var enumName: (Enum<*>) -> String = { it.name }

    var allIdentifiers = emptyList<Identifier>()
}

class DataComponent<T>(gui: FeatureGui, val data: PropertyData<T>) : Setting() {

    private val boundingBox: UIBlock by UIBlock(ColorCache.darkHighlight).constrain {
        x = 1.pixel()
        y = 1.pixel()

        width = 100.percent() - 10.pixels()

        val bottom =
            if (data.componentType == ComponentType.SELECTOR) basicHeightConstraint { textBoundingBox.getHeight() } else ChildBasedMaxSizeConstraint()

        height = bottom + 15.pixels()
    } childOf this effect OutlineEffect(ColorCache.divider, 1f)

    init {
        var addedButtons = 0

        data.componentType?.getComponent(data)?.let {
            it childOf boundingBox
            it.setupParentListeners(this)

            addedButtons++
        }

        if (data.settings.isNotEmpty()) {
            ButtonComponent("Settings") {
                gui.addComponentToMainSettings(CategoryFeatures(gui, data.settings).constrain {
                    width = 100.percent()
                    height = 100.percent()
                })
            }.constrain {
                x = getXConstraint(addedButtons++)
            } childOf boundingBox
        }
    }

    private fun getXConstraint(addedButtons: Int) =
        if (addedButtons == 0) 15.pixels(alignOpposite = true) else SiblingConstraint(15f, true)

    private val textBoundingBox by UIContainer().constrain {
        x = 15.pixels()
        y = 15.pixels()

        width = basicWidthConstraint {
            val end =
                (boundingBox.children - it).map { element -> element.getLeft() }.minOrNull() ?: boundingBox.getRight()

            end - it.getLeft() - 10F
        }
        height = ChildBasedSizeConstraint(3f) + 15.pixels()
    } childOf boundingBox

    init {
        UIWrappedText(data.title).constrain {
            width = 100.percent()

            textScale = 1.49.pixels()
            color = ColorCache.brightText.constraint
        } childOf textBoundingBox

        UIWrappedText(data.desc).constrain {
            y = SiblingConstraint() + 3.pixels()
            width = 100.percent()
            color = ColorCache.midText.constraint
        } childOf textBoundingBox
    }
}