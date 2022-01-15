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

import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import kotlin.reflect.KMutableProperty0

abstract class AbstractDefaultFeature(val key: String, val title: String, val desc: String) {
    val parameters = hashMapOf<String, FeatureParameter<*>>()

    abstract fun getComponentType(): ComponentType?

    abstract fun getProperty(): KMutableProperty0<*>

    abstract fun toPropertyData(): PropertyData<*>

    init {
        allDefaultFeatures.add(this)
    }

    companion object {
        val allDefaultFeatures = arrayListOf<AbstractDefaultFeature>()
    }
}