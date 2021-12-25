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

open class ConfigMap<V>(
    private val category: String,
    defaultValue: V,
    private val converter: Converter<V>
) {

    private val internalMap =
        ConfigHandler.getKeys(category).associateWith { ConfigHandler.get(category, it, defaultValue, converter) }
            .toMutableMap()

    fun clear() {
        ConfigHandler.deleteCategory(category)
        internalMap.clear()
    }

    fun remove(key: String): V? {
        ConfigHandler.deleteKey(category, key)

        return internalMap.remove(key)
    }

    operator fun get(key: String) = internalMap[key]

    operator fun set(key: String, value: V) {
        ConfigHandler.write(category, key, value, converter)
        internalMap[key] = value
    }

    fun getOrPut(key: String, defaultValue: () -> V) = internalMap.getOrPut(key, defaultValue)

    class DoubleConfigMap(category: String) :
        ConfigMap<Double>(
            category,
            0.0,
            CDouble
        )

    class IntConfigMap(category: String) :
        ConfigMap<Int>(
            category,
            0,
            CInt
        )


    class BooleanConfigMap(category: String) :
        ConfigMap<Boolean>(
            category,
            false,
            CBoolean
        )

    class StringConfigMap(category: String) :
        ConfigMap<String>(
            category,
            "",
            CString
        )
}