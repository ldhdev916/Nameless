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
) : HashMap<String, V>() {

    init {
        for (key in ConfigHandler.getKeys(category)) {
            super.put(key, ConfigHandler.get(category, key, defaultValue, converter))
        }
    }

    override fun remove(key: String): V? {
        ConfigHandler.deleteKey(category, key)
        return super.remove(key)
    }

    override fun remove(key: String, value: V): Boolean {
        ConfigHandler.deleteKey(category, key)
        return super.remove(key, value)
    }

    override fun put(key: String, value: V): V? {
        ConfigHandler.write(category, key, value, converter)
        return super.put(key, value)
    }

    override fun clear() {
        ConfigHandler.deleteCategory(category)
        super.clear()
    }

    @Deprecated("Not Supported", ReplaceWith("this.put(value)", "java.util.HashMap"))
    override fun putAll(from: Map<out String, V>) {
        super.putAll(from)
    }

    @Deprecated("Not Supported", ReplaceWith("this.put(value)", "java.util.HashMap"))
    override fun putIfAbsent(key: String, value: V): V? {
        return super.putIfAbsent(key, value)
    }

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
