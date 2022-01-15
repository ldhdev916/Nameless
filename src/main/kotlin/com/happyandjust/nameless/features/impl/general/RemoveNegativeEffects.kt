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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.serialization.converters.CList
import com.happyandjust.nameless.serialization.converters.getEnumConverter

object RemoveNegativeEffects : SimpleFeature(
    Category.GENERAL,
    "removenegativeeffects",
    "Remove Negative Effects",
    "Support Blindness, Nausea"
) {

    var enabledPotionTypes by object : FeatureParameter<List<PotionType>>(
        0, "removenegativeeffects", "potiontypes", "Potion Types", "", PotionType.values().toList(), CList(
            getEnumConverter()
        )
    ) {
        init {
            allEnumList = PotionType.values().toList()
            enumName = {
                val name = (it as PotionType).name

                "${name[0]}${name.drop(1).lowercase()}"
            }
        }

        override fun getComponentType() = ComponentType.MULTI_SELECTOR
    }

    enum class PotionType {
        BLINDNESS, NAUSEA
    }
}