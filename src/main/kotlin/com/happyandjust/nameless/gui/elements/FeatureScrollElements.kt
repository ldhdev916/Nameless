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
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EFeaturePanel(
    rectangle: Rectangle,
    private val panelStacks: EPanelStacks
) : EScrollPanel(rectangle, 10, emptyList()) {

    fun filter(f: (SimpleFeature) -> Boolean) {
        val newElements = with(rectangle) {
            val features = arrayListOf<EPanel>()

            val inCategorySorted = hashMapOf<String, ArrayList<SimpleFeature>>()

            for (feature in FeatureRegistry.features) { // sort by in category

                if (!f(feature)) continue // filter here

                val list = inCategorySorted[feature.inCategory] ?: arrayListOf()

                list.add(feature)

                inCategorySorted[feature.inCategory] = list
            }

            for ((_, list) in inCategorySorted) { // sort by alphabetical
                list.sortBy { it.title }
            }

            val inCategorySortedEntries =
                inCategorySorted.entries.sortedBy { it.key } // sort incategory by alphabetical

            for ((inCategory, featureList) in inCategorySortedEntries) {

                if (inCategory.isNotBlank()) {
                    features.add(EInCategory(this@EFeaturePanel, inCategory))
                }

                for (feature in featureList) {
                    val descSize = if (feature.desc.isBlank()) 0 else feature.desc.split("\n").size

                    features.add(
                        EFeature(
                            Rectangle(
                                left + 4,
                                0,
                                right - 4,
                                (mc.fontRendererObj.FONT_HEIGHT * 4) + (descSize * mc.fontRendererObj.FONT_HEIGHT) + 6
                            ),
                            feature,
                            panelStacks,
                            rectangle
                        )
                    )
                }
            }

            features
        }

        updateElements(newElements)
    }
}

class EFeatureSettingPanel(
    rectangle: Rectangle,
    private val feature: SimpleFeature,
    private val panelStacks: EPanelStacks
) : EScrollPanel(rectangle, 10, emptyList()) {

    init {
        filter { true }
    }

    fun filter(f: (FeatureParameter<*>) -> Boolean) {
        // we need to update element here because we can't use 'this' in constructor
        with(rectangle) {
            val params = arrayListOf<EPanel>()

            val sortedParams = feature.parameters.values.sortedWith(compareBy({ it.ordinal }, { it.title }))

            val inCategorySorted = hashMapOf<String, ArrayList<FeatureParameter<*>>>()

            for (parameter in sortedParams) { // sort by in category

                if (!f(parameter)) continue // filter

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
                            parameter,
                            panelStacks,
                            rectangle
                        )
                    )
                }
            }

            updateElements(params)
        }
    }
}

class EParameterSettingPanel(
    rectangle: Rectangle,
    private val featureParameter: FeatureParameter<*>,
    private val panelStacks: EPanelStacks
) : EScrollPanel(rectangle, 10, emptyList()) {

    init {
        filter { true }
    }

    fun filter(f: (FeatureParameter<*>) -> Boolean) {
        with(rectangle) {
            val params = arrayListOf<EPanel>()

            val sortedParams = featureParameter.parameters.values.sortedWith(compareBy({ it.ordinal }, { it.title }))

            val inCategorySorted = hashMapOf<String, ArrayList<FeatureParameter<*>>>()

            for (parameter in sortedParams) {

                if (!f(parameter)) continue

                val list = inCategorySorted[parameter.inCategory] ?: arrayListOf()

                list.add(parameter)

                inCategorySorted[parameter.inCategory] = list
            }

            val inCategorySortedEntries = inCategorySorted.entries.sortedBy { it.key }

            val fontHeight = mc.fontRendererObj.FONT_HEIGHT

            for ((inCategory, parameters) in inCategorySortedEntries) {
                if (inCategory.isNotBlank()) {
                    params.add(EInCategory(this@EParameterSettingPanel, inCategory))
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
                            parameter,
                            panelStacks,
                            rectangle
                        )
                    )
                }
            }

            updateElements(params)
        }
    }
}