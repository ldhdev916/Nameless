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
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.serialization.Converter

open class FeatureParameter<T>(
    val ordinal: Int,
    category: String,
    key: String,
    title: String,
    desc: String,
    private val defaultValue: T,
    converter: Converter<T>
) : AbstractDefaultFeature(key, title, desc) {
    var inCategory = ""

    var onValueChange: (T) -> Unit = {}

    var allEnumList = emptyList<Enum<*>>()
    var enumName: (Enum<*>) -> String = { it.name }

    var validator: (Char) -> Boolean = { true }
    var placeHolder = ""

    var minValue: Double = 0.0
    var maxValue: Double = 0.0

    var allIdentifiers = emptyList<Identifier>()

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

    fun digIntoParameter(): List<FeatureParameter<*>> {

        val list = arrayListOf<FeatureParameter<*>>()

        list.add(this)
        parameters.values.forEach { list.addAll(it.digIntoParameter()) }

        return list
    }

    fun getParentDefaultFeature() = getFeatureAndSubParameters().first { it.parameters.containsValue(this) }

    override fun getComponentType(): ComponentType? = when {
        checkType<Int>() -> ComponentType.SLIDER
        checkType<Double>() -> ComponentType.SLIDER_DECIMAL
        checkType<Boolean>() -> ComponentType.SWITCH
        checkType<String>() -> ComponentType.TEXT
        checkType<ChromaColor>() -> ComponentType.COLOR
        checkType<Enum<*>>() -> ComponentType.SELECTOR
        checkType<List<Identifier>>() -> ComponentType.VERTIAL_MOVE
        checkType<() -> Unit>() -> ComponentType.BUTTON
        else -> throw IllegalArgumentException("Unable to find appropriate component type for class ${defaultValue!!.javaClass.name}")
    }

    private inline fun <reified E> checkType() = defaultValue is E

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
        it.enumName = enumName

        it.allIdentifiers = allIdentifiers

        it.placeHolder = placeHolder

        it.settings = parameters.values.map { featureParameter -> featureParameter.toPropertyData() }
    }
}


