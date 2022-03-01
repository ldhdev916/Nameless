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

package com.happyandjust.nameless.config

import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

open class ConfigMap<V>(
    private val category: String,
    private val serializer: KSerializer<V>
) : MutableMap<String, V> {

    private val internalMap =
        ConfigHandler.getKeys(category).associateWith { ConfigHandler.get(category, it, serializer) }
            .toMutableMap()

    override fun get(key: String) = internalMap[key]

    override fun clear() {
        ConfigHandler.deleteCategory(category)
        internalMap.clear()
    }

    override fun remove(key: String): V? {
        ConfigHandler.deleteKey(category, key)
        return internalMap.remove(category)
    }

    override val size: Int
        get() = internalMap.size

    override fun containsKey(key: String) = internalMap.containsKey(key)
    override fun containsValue(value: V) = internalMap.containsValue(value)

    override fun isEmpty() = internalMap.isEmpty()

    override val entries: MutableSet<MutableMap.MutableEntry<String, V>>
        get() = internalMap.entries
    override val keys: MutableSet<String>
        get() = internalMap.keys
    override val values: MutableCollection<V>
        get() = internalMap.values

    override fun put(key: String, value: V): V? {
        ConfigHandler.write(category, key, value, serializer)
        return internalMap.put(key, value)
    }

    override fun putAll(from: Map<out String, V>) {
        from.forEach(::put)
    }
}

inline fun <reified V> configMap(category: String) = ConfigMap<V>(category, serializer())