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

import com.happyandjust.nameless.core.input.UserInputItem
import com.happyandjust.nameless.gui.feature.ComponentType
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

inline fun <reified T : Any, P : FeatureParameter<T>> BaseFeature<*>.parameterDefault(
    defaultValue: T,
    serializer: KSerializer<T>,
    init: (T, KSerializer<T>) -> P,
    builder: P.() -> Unit
) = init(defaultValue, serializer).apply {
    componentType = ComponentType.values().find { it.isProperData(defaultValue) }

    builder()

    if (!parameterCategoryInitialized) {
        matchKeyCategory()
    }
}

inline fun <reified T : Any> BaseFeature<*>.overlayParameter(
    defaultValue: T,
    serializer: KSerializer<T> = serializer(),
    builder: OverlayParameter<T>.() -> Unit
) = parameterDefault(
    defaultValue = defaultValue,
    serializer = serializer,
    init = ::OverlayParameter,
    builder = builder
)

inline fun <reified T : Any> BaseFeature<*>.parameter(
    defaultValue: T,
    serializer: KSerializer<T> = serializer(),
    builder: FeatureParameter<T>.() -> Unit = {}
) = parameterDefault(
    defaultValue = defaultValue,
    serializer = serializer,
    init = ::FeatureParameter,
    builder = builder
)

inline fun BaseFeature<*>.userInputParameter(
    defaultValue: UserInputItem,
    builder: FeatureParameter<UserInputItem>.() -> Unit = {}
) = parameter(defaultValue, UserInputItem.serializer(), builder)