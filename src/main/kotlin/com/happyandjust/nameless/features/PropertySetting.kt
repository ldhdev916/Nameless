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

@file:Suppress("unused")

package com.happyandjust.nameless.features

import com.happyandjust.nameless.core.input.InputPlaceHolder
import com.happyandjust.nameless.dsl.listEnum
import com.happyandjust.nameless.features.base.AbstractDefaultFeature
import com.happyandjust.nameless.gui.feature.components.Identifier

class PropertySetting {
    var ordinal = 0
    var subCategory = ""

    var placeHolder: String? = null

    var validator: (Char) -> Boolean = { true }

    var minValue = 0.0
    var maxValue = 0.0
    var minValueInt = 0
    var maxValueInt = 0

    var allIdentifiers = emptyList<Identifier>()

    var stringSerializer: (Any) -> String = { it.javaClass.name }
    var allValueList = { emptyList<Any>() }

    var registeredPlaceHolders = emptyList<InputPlaceHolder>()

    inline fun <reified T : Any> AbstractDefaultFeature<T>.serializer(crossinline value: (T) -> String) {
        stringSerializer = {
            value(it as T)
        }
    }

    inline fun <reified T : Any, E : List<T>> AbstractDefaultFeature<E>.listSerializer(crossinline value: (T) -> String) {
        stringSerializer = { value(it as T) }
    }

    inline fun <reified T : Enum<T>> AbstractDefaultFeature<T>.autoFillEnum(
        noinline allValueList: () -> List<T> = { listEnum() },
        crossinline stringSerializer: (T) -> String = { it.name }
    ) {
        serializer(stringSerializer)
        this@PropertySetting.allValueList = allValueList

    }

    @JvmName("listAutoFillEnum")
    inline fun <reified T : Enum<T>, E : List<T>> AbstractDefaultFeature<E>.autoFillEnum(
        noinline allValueList: () -> List<T> = { listEnum() },
        crossinline stringSerializer: (T) -> String = { it.name }
    ) {
        listSerializer(stringSerializer)
        this@PropertySetting.allValueList = allValueList
    }
}

inline fun AbstractDefaultFeature<*>.settings(builder: PropertySetting.() -> Unit) {
    propertySetting.builder()
}