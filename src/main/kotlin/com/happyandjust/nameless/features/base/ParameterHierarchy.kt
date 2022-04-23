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

import com.happyandjust.nameless.features.settings
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible


class ParameterHierarchy(val featureOfHierarchy: AbstractDefaultFeature<*>) {
    var parent: ParameterHierarchy? = null
    var autoSetOrdinal = true
    private var lastOrdinal = 0

    fun KMutableProperty0<*>.getDelegateParameter(): FeatureParameter<*> {
        isAccessible = true
        return getDelegate() as FeatureParameter<*>
    }

    operator fun KMutableProperty0<*>.unaryPlus() {
        +getDelegateParameter()
    }

    operator fun FeatureParameter<*>.unaryPlus() {
        featureOfHierarchy.parameters[key] = this
        hierarchy.parent = this@ParameterHierarchy

        if (autoSetOrdinal) {
            settings {
                ordinal = lastOrdinal++
            }
        }
    }

    operator fun KMutableProperty0<*>.unaryMinus() {
        -getDelegateParameter()
    }

    operator fun FeatureParameter<*>.unaryMinus() {
        featureOfHierarchy.parameters.remove(key, this)
        hierarchy.parent = null
    }

    inline operator fun KMutableProperty0<*>.invoke(action: ParameterHierarchy.() -> Unit) {
        getDelegateParameter()(action)
    }

    inline operator fun FeatureParameter<*>.invoke(action: ParameterHierarchy.() -> Unit) {
        +this
        hierarchy.action()
    }

    companion object {
        private val hierarchySetupCallbacks = hashSetOf<() -> Unit>()

        fun executeAll() {
            hierarchySetupCallbacks.forEach { it() }
            hierarchySetupCallbacks.clear()
        }

        fun add(action: () -> Unit) {
            hierarchySetupCallbacks.add(action)
        }
    }
}

inline fun BaseFeature<*>.hierarchy(crossinline action: ParameterHierarchy.() -> Unit) {
    ParameterHierarchy.add {
        hierarchy.action()
    }
}

inline fun BaseFeature<*>.executeHierarchy(action: ParameterHierarchy.() -> Unit) {
    hierarchy.action()
}

inline fun ParameterHierarchy.nonOrdinal(action: ParameterHierarchy.() -> Unit) {
    autoSetOrdinal = false
    action()
    autoSetOrdinal = true
}