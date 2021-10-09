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

package com.happyandjust.nameless.features

import com.happyandjust.nameless.config.ConfigHandler
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.serialization.Converter
import com.happyandjust.nameless.textureoverlay.ERelocateGui

/**
 * @param ordinal only for sorting in [com.happyandjust.nameless.gui.elements.EFeatureSettingPanel]
 */
class FeatureParameter<T>(
    val ordinal: Int,
    category: String,
    key: String,
    val title: String,
    val desc: String,
    val defaultValue: T,
    converter: Converter<T>
) {

    val parameters = hashMapOf<String, FeatureParameter<*>>()
    var relocateGui: () -> ERelocateGui? = { null }
    var allEnumList = emptyList<Enum<*>>()
    var maxStringWidth = 100
    var validator: (String) -> Boolean = { true }
    var inCategory = ""
    var minValue: Double = 0.0
    var maxValue: Double = 0.0
    private var valueConfig =
        ConfigValue(category, key, defaultValue, { s, k, v -> ConfigHandler.get(s, k, v, converter) }) { s, k, v ->

            ConfigHandler.write(s, k, converter.serialize(v))
        }
    var value: T
        get() = valueConfig.value
        set(value) {
            valueConfig.value = value
        }

    fun <T> getParameter(key: String) = parameters[key] as FeatureParameter<T>

    fun <T> getParameterValue(key: String) = getParameter<T>(key).value
}


