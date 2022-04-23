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

package com.happyandjust.nameless.core.property

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.features.PropertySetting
import com.happyandjust.nameless.gui.feature.ComponentType
import kotlin.reflect.KMutableProperty0


data class PropertyData(
    val propertyValue: PropertyValue,
    val title: String,
    val desc: String,
    val componentType: ComponentType?,
    val propertySetting: PropertySetting,
    val settings: List<PropertyData> = emptyList()
)

interface PropertyValue {
    fun getValue(): Any

    fun setValue(value: Any)
}

data class KPropertyBackedPropertyValue<T : Any>(private val property: KMutableProperty0<T>) : PropertyValue {
    override fun getValue(): Any {
        return property()
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any) {
        property.set(value as T)
    }
}

data class ConfigBackedPropertyValue<T : Any>(private val config: ConfigValue<T>) : PropertyValue {
    override fun getValue(): Any {
        return config.value
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any) {
        config.value = value as T
    }
}