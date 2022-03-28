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

import com.happyandjust.nameless.core.property.PropertyData
import com.happyandjust.nameless.core.property.PropertyValue
import com.happyandjust.nameless.features.PropertySetting
import com.happyandjust.nameless.gui.feature.ComponentType

abstract class AbstractDefaultFeature<T : Any> {
    val parameters = object : HashMap<String, FeatureParameter<*>>() {
        override fun put(key: String, value: FeatureParameter<*>): FeatureParameter<*>? {
            value.parent = this@AbstractDefaultFeature
            return super.put(key, value)
        }
    }
    var parent: AbstractDefaultFeature<*>? = null
    abstract var componentType: ComponentType?
    abstract val propertyValue: PropertyValue

    lateinit var key: String
    lateinit var title: String
    var desc = ""

    val propertySetting = PropertySetting()


    open fun toPropertyData(): PropertyData = PropertyData(
        propertyValue,
        title,
        desc,
        componentType,
        propertySetting,
        parameters.values.map { it.toPropertyData() }
    )

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getParameter(key: String): FeatureParameter<T> {
        val split = key.split("/")
        val first = parameters[split.first()]!!
        val remainKey = split.drop(1).joinToString("/").ifEmpty {
            return first as FeatureParameter<T>
        }

        return first.getParameter(remainKey)
    }

    fun <T : Any> getParameterValue(key: String) = getParameter<T>(key).value

    fun hasParameter(key: String) = key in parameters

    init {
        allDefaultFeatures.add(this)
    }

    companion object {
        val allDefaultFeatures = arrayListOf<AbstractDefaultFeature<*>>()
    }
}