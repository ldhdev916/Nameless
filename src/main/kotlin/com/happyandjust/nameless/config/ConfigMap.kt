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

import com.happyandjust.nameless.serialization.TypeRegistry

open class ConfigMap<V>(
    private val category: String,
    configMethod: (String, String) -> V,
    private val saveMethod: (String, String, V) -> Unit
) : HashMap<String, V>() {

    init {
        for (key in ConfigHandler.getKeys(category)) {
            super.put(key, configMethod(category, key))
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
        saveMethod(category, key, value)
        return super.put(key, value)
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
            { s, k -> ConfigHandler.get(s, k, 0.0, TypeRegistry.getConverterByClass(Double::class)) },
            ConfigHandler::write
        )

    class IntConfigMap(category: String) :
        ConfigMap<Int>(
            category,
            { s, k -> ConfigHandler.get(s, k, 0, TypeRegistry.getConverterByClass(Int::class)) },
            ConfigHandler::write
        )


    class BooleanConfigMap(category: String) :
        ConfigMap<Boolean>(
            category,
            { s, k -> ConfigHandler.get(s, k, false, TypeRegistry.getConverterByClass(Boolean::class)) },
            ConfigHandler::write
        )

    class StringConfigMap(category: String) :
        ConfigMap<String>(
            category,
            { c, key -> ConfigHandler.get(c, key, "", TypeRegistry.getConverterByClass(String::class)) },
            ConfigHandler::write
        )
}
