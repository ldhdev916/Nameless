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

package com.happyandjust.nameless.gui.elements

import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EFeatureSettingPanel(
    rectangle: Rectangle,
    feature: SimpleFeature,
    val onSettingButtonClicked: (FeatureParameter<*>) -> Unit
) : EScrollPanel(rectangle, 10, emptyList()) {

    init {
        // we need to update element here because we can't use 'this' in constructor
        with(rectangle) {
            val params = arrayListOf<EPanel>()

            val sortedParams = feature.parameters.values.sortedWith(compareBy({ it.ordinal }, { it.title }))

            val inCategorySorted = hashMapOf<String, ArrayList<FeatureParameter<*>>>()

            for (parameter in sortedParams) { // sort by in category
                val list = inCategorySorted[parameter.inCategory] ?: arrayListOf()

                list.add(parameter)

                inCategorySorted[parameter.inCategory] = list
            }

            val inCategorySortedEntries = inCategorySorted.entries.sortedBy { it.key } // sort by alphabetical

            val fontHeight = mc.fontRendererObj.FONT_HEIGHT

            for ((inCategory, parameters) in inCategorySortedEntries) {

                if (inCategory.isNotBlank()) {
                    params.add(EInCategory(this@EFeatureSettingPanel, inCategory)) // this part
                }

                for (parameter in parameters) {
                    val descSize = if (parameter.desc.isBlank()) 0 else parameter.desc.split("\n").size

                    params.add(
                        EParameter(
                            Rectangle(
                                left + 4,
                                0,
                                right - 4,
                                fontHeight * (descSize + 4) + 6
                            ),
                            parameter
                        ) { onSettingButtonClicked(it.parameter) }
                    )
                }
            }

            updateElements(params)
        }
    }
}