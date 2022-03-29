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