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

package com.happyandjust.nameless.features

import com.happyandjust.nameless.features.base.AbstractDefaultFeature
import com.happyandjust.nameless.gui.feature.components.Identifier

open class PropertySetting<T : Any, E : Any> {
    var ordinal = 0
    var subCategory = ""

    var placeHolder: String? = null

    var validator: (Char) -> Boolean = { true }

    var minValue = 0.0
    var maxValue = 0.0
    var minValueInt = 0
    var maxValueInt = 0

    var stringSerializer: (T) -> String = { it.javaClass.name }
    var allValueList = { emptyList<T>() }

    var listStringSerializer: (E) -> String = { it.javaClass.name }
    var listAllValueList = { emptyList<E>() }

    var allIdentifiers = emptyList<Identifier>()
}

inline fun <T : Any, E : Any> AbstractDefaultFeature<T, E>.settings(settingBuilder: PropertySetting<T, E>.() -> Unit) {
    propertySetting.settingBuilder()
}