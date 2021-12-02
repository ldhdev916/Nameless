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

package com.happyandjust.nameless.config

import com.happyandjust.nameless.serialization.Converter
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CDouble
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.serialization.converters.CString
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class ConfigValue<T>(
    private val category: String,
    private val key: String,
    defaultValue: T,
    private val converter: Converter<T>
) : ReadWriteProperty<Any?, T> {

    var value: T = ConfigHandler.get(category, key, defaultValue, converter)
        set(value) {
            if (value != null) {
                ConfigHandler.write(category, key, value, converter)
                field = value
            }
        }


    class BooleanConfigValue(category: String, key: String, defaultValue: Boolean) : ConfigValue<Boolean>(
        category,
        key,
        defaultValue,
        CBoolean
    )

    class DoubleConfigValue(category: String, key: String, defaultValue: Double) : ConfigValue<Double>(
        category,
        key,
        defaultValue,
        CDouble
    )

    class IntConfigValue(category: String, key: String, defaultValue: Int) : ConfigValue<Int>(
        category,
        key,
        defaultValue,
        CInt
    )

    class StringConfigValue(category: String, key: String, defaultValue: String) : ConfigValue<String>(
        category,
        key,
        defaultValue,
        CString
    )

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }
}