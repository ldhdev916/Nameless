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

package com.happyandjust.nameless.features.base

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.value.ChromaColor
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.serialization.Converter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.functions

open class FeatureParameter<T>(
    var ordinal: Int,
    category: String,
    key: String,
    title: String,
    desc: String,
    private val defaultValue: T,
    converter: Converter<T>
) : AbstractDefaultFeature(key, title, desc), ReadWriteProperty<AbstractDefaultFeature, T> {
    var inCategory = ""

    var onValueChange: (T) -> Unit = {}

    var enumName: (Enum<*>) -> String = { it.name }
    var allEnumList = emptyList<Enum<*>>()
    private val reflectionEnumList by lazy {
        (defaultValue!!::class.functions.first { it.name == "values" }.call() as Array<Enum<*>>).toList()
    }

    var validator: (Char) -> Boolean = { true }
    var placeHolder = ""

    var minValue: Double = 0.0
    var maxValue: Double = 0.0

    var allIdentifiers = emptyList<Identifier>()

    private val valueConfig =
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

        it.enumName = enumName
        it.allEnumList = allEnumList.ifEmpty { if (defaultValue is Enum<*>) reflectionEnumList else emptyList() }

        it.allIdentifiers = allIdentifiers

        it.placeHolder = placeHolder

        it.settings = parameters.values.map { featureParameter -> featureParameter.toPropertyData() }
    }


    override fun getValue(thisRef: AbstractDefaultFeature, property: KProperty<*>) = value

    override fun setValue(thisRef: AbstractDefaultFeature, property: KProperty<*>, value: T) {
        this.value = value
    }
}