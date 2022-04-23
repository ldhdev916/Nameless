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

package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.core.input.UserInputItem
import com.happyandjust.nameless.dsl.TempEventListener
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.ParameterHierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.base.userInputParameter
import com.happyandjust.nameless.features.impl.qol.MurdererFinder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

interface MurdererMode : TempEventListener {
    fun isEnabled(): Boolean
}

interface MurdererModeCreator {
    fun createImpl(): MurdererMode

    val modes: Iterable<String>

    fun ParameterHierarchy.setupHierarchy()
}

inline fun <reified T : Any> MurdererModeCreator.parameter(
    defaultValue: T,
    serializer: KSerializer<T> = serializer(),
    builder: FeatureParameter<T>.() -> Unit = {}
) = MurdererFinder.parameter(defaultValue, serializer, builder)

inline fun MurdererModeCreator.userInputParameter(
    defaultValue: UserInputItem,
    builder: FeatureParameter<UserInputItem>.() -> Unit = {}
) = MurdererFinder.userInputParameter(defaultValue, builder)