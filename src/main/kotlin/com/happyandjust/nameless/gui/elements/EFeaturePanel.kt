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
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.gui.EPanel
import com.happyandjust.nameless.gui.Rectangle

class EFeaturePanel(
    rectangle: Rectangle,
    val onFeatureSettingButtonClicked: (SimpleFeature) -> Unit
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
                            feature
                        ) { onFeatureSettingButtonClicked(it.feature) }
                    )
                }
            }

            features
        }

        updateElements(newElements)
    }
}