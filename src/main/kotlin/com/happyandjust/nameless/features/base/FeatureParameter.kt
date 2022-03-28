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
import com.happyandjust.nameless.core.property.KPropertyBackedPropertyValue
import com.happyandjust.nameless.core.property.PropertyValue
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.gui.feature.ComponentType
import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class FeatureParameter<T : Any>(
    private val defaultValue: T,
    serializer: KSerializer<T>
) : AbstractDefaultFeature<T>(), ReadWriteProperty<Any?, T> {

    private val jsonSaveKey: String
        get() = buildString {
            withInstance<FeatureParameter<*>>(parent) {
                append(jsonSaveKey)
                append("_")
            }
            append(key)
        }

    lateinit var category: String

    val parameterCategoryInitialized
        get() = ::category.isInitialized

    var onValueChange: (T) -> Unit = {}
        private set

    override var componentType: ComponentType? = null

    var value by object : ReadWriteProperty<FeatureParameter<T>, T> {

        val lazyConfig by lazy { ConfigValue(category, jsonSaveKey, defaultValue, serializer) }

        override fun getValue(thisRef: FeatureParameter<T>, property: KProperty<*>): T {
            return lazyConfig.value
        }

        override fun setValue(thisRef: FeatureParameter<T>, property: KProperty<*>, value: T) {
            if (lazyConfig.value != value) {
                lazyConfig.value = value
                onValueChange(value)
            }
        }
    }

    override val propertyValue: PropertyValue by lazy { KPropertyBackedPropertyValue(::value) }

    fun onValueChange(action: (T) -> Unit) {
        onValueChange = action
    }

    fun BaseFeature<*>.matchKeyCategory() {
        this@FeatureParameter.category = key
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}