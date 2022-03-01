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
import com.happyandjust.nameless.dsl.listEnum
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.ComponentType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.jvm.internal.TypeIntrinsics
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class FeatureParameter<T : Any, E : Any>(
    private val defaultValue: T,
    serializer: KSerializer<T>
) : AbstractDefaultFeature<T, E>() {

    private val jsonSaveKey: String
        get() = buildList {
            add(key)
            var parent = parent
            while (parent != null) {
                add(parent.key)
                parent = parent.parent
            }
        }.reversed().joinToString("_")

    lateinit var category: String

    var onValueChange: (T) -> Unit = {}
        private set

    private var preferComponentType: ComponentType? = null
        get() = if (!hasPrefer) error("Unable to find appropriate component type for class ${defaultValue.javaClass.name}") else field
    private var hasPrefer = false

    override var componentType: ComponentType?
        get() = when (defaultValue) {
            is Int -> ComponentType.SLIDER
            is Double -> ComponentType.SLIDER_DECIMAL
            is Boolean -> ComponentType.SWITCH
            is String -> ComponentType.TEXT
            is ChromaColor -> ComponentType.COLOR
            is Enum<*> -> ComponentType.SELECTOR
            is List<*> -> if (propertySetting.allIdentifiers.isNotEmpty()) ComponentType.VERTIAL_MOVE else ComponentType.MULTI_SELECTOR
            else -> if (TypeIntrinsics.isFunctionOfArity(defaultValue, 0)) ComponentType.BUTTON else preferComponentType
        }
        set(value) {
            preferComponentType = value
            hasPrefer = true
        }

    var value by object : ReadWriteProperty<FeatureParameter<T, E>, T> {

        val lazyConfig by lazy { ConfigValue(category, jsonSaveKey, defaultValue, serializer) }

        override fun getValue(thisRef: FeatureParameter<T, E>, property: KProperty<*>): T {
            return lazyConfig.value
        }

        override fun setValue(thisRef: FeatureParameter<T, E>, property: KProperty<*>, value: T) {
            if(lazyConfig.value != value) {
                lazyConfig.value = value
                onValueChange(value)
            }
        }
    }

    override val property
        get() = ::value

    fun onValueChange(action: (T) -> Unit) {
        onValueChange = action
    }

    fun SimpleFeature.matchKeyCategory() {
        this@FeatureParameter.category = key
    }
}

inline fun <reified T : Any> AbstractDefaultFeature<*, *>.parameter(
    defaultValue: T,
    serializer: KSerializer<T> = serializer(),
    builderAction: FeatureParameter<T, Any>.() -> Unit = {}
) = FeatureParameter<T, Any>(defaultValue, serializer).apply {
    builderAction()

    this@parameter.parameters[key] = this
}

inline fun <E : Any, reified T : List<E>> AbstractDefaultFeature<*, *>.listParameter(
    defaultValue: T,
    serializer: KSerializer<T> = serializer(),
    builderAction: FeatureParameter<T, E>.() -> Unit = {}
) = FeatureParameter<T, E>(defaultValue, serializer).apply {
    builderAction()

    this@listParameter.parameters[key] = this
}

inline fun <reified T : Enum<T>, E : Any> AbstractDefaultFeature<T, E>.autoFillEnum(
    noinline allValueList: () -> List<T> = { listEnum() },
    noinline stringSerializer: (T) -> String = { it.name }
) = settings {
    this.allValueList = allValueList
    this.stringSerializer = stringSerializer
}

@JvmName("listAutoFillEnum")
inline fun <reified E : Enum<E>, T : List<E>> AbstractDefaultFeature<T, E>.autoFillEnum(
    noinline listAllValueList: () -> List<E> = { listEnum() },
    noinline listStringSerializer: (E) -> String = { it.name }
) = settings {
    this.listAllValueList = listAllValueList
    this.listStringSerializer = listStringSerializer
}