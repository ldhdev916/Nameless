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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import com.happyandjust.nameless.serialization.Converter

open class FeatureParameter<T>(
    val ordinal: Int,
    category: String,
    key: String,
    val title: String,
    val desc: String,
    private val defaultValue: T,
    converter: Converter<T>
) : IHasComponentType {

    val parameters = hashMapOf<String, FeatureParameter<*>>()
    var inCategory = ""

    var onValueChange: (T) -> Unit = {}

    var allEnumList = emptyList<Enum<*>>()

    var validator: (Char) -> Boolean = { true }
    var placeHolder = ""

    var minValue: Double = 0.0
    var maxValue: Double = 0.0

    private var valueConfig =
        ConfigValue(category, key, defaultValue, converter)
    var value = valueConfig.value
        set(value) {
            if (field != value) {
                field = value
                valueConfig.value = value

                onValueChange(value)
            }
        }

    fun <T> getParameter(key: String) = parameters[key] as FeatureParameter<T>

    fun <T> getParameterValue(key: String) = getParameter<T>(key).value

    override fun getComponentType() = when (defaultValue) {
        is Int -> ComponentType.SLIDER
        is Double -> ComponentType.SLIDER_DECIMAL
        is Boolean -> ComponentType.SWITCH
        is String -> ComponentType.TEXT
        is ChromaColor -> ComponentType.COLOR
        is Enum<*> -> ComponentType.SELECTOR
        else -> throw IllegalArgumentException("Unable to find appropriate component type for class ${defaultValue!!.javaClass.name}")
    }

    override fun getProperty() = ::value

    override fun toPropertyData(): PropertyData<*> = PropertyData(
        getProperty(),
        title,
        desc,
        getComponentType()
    ).also {
        it.inCategory = inCategory

        it.minValue = minValue
        it.maxValue = maxValue

        it.ordinal = ordinal

        it.validator = validator

        it.allEnumList = allEnumList

        it.relocateAble = this as? IRelocateAble

        it.settings = parameters.values.map { featureParameter -> featureParameter.toPropertyData() }
    }
}


