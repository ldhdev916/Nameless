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

open class ConfigValue<T>(
    private val category: String,
    private val key: String,
    defaultValue: T,
    configMethod: (String, String, T) -> T,
    private val saveMethod: (String, String, T) -> Unit
) {

    var value: T = configMethod(category, key, defaultValue)
        set(value) {
            value?.let {
                saveMethod(category, key, it)
                field = it
            }
        }


    class BooleanConfigValue(category: String, key: String, defaultValue: Boolean) : ConfigValue<Boolean>(
        category,
        key,
        defaultValue,
        { s, k, v -> ConfigHandler.get(s, k, v, TypeRegistry.getConverterByClass(Boolean::class)) },
        ConfigHandler::write
    )

    class DoubleConfigValue(category: String, key: String, defaultValue: Double) : ConfigValue<Double>(
        category,
        key,
        defaultValue,
        { s, k, v -> ConfigHandler.get(s, k, v, TypeRegistry.getConverterByClass(Double::class)) },
        ConfigHandler::write
    )

    class IntConfigValue(category: String, key: String, defaultValue: Int) : ConfigValue<Int>(
        category,
        key,
        defaultValue,
        { s, k, v -> ConfigHandler.get(s, k, v, TypeRegistry.getConverterByClass(Int::class)) },
        ConfigHandler::write
    )

    class StringConfigValue(category: String, key: String, defaultValue: String) : ConfigValue<String>(
        category,
        key,
        defaultValue,
        { s, k, v -> ConfigHandler.get(s, k, v, TypeRegistry.getConverterByClass(String::class)) },
        ConfigHandler::write
    )
}
