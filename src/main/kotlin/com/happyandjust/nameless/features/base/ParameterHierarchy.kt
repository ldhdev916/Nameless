package com.happyandjust.nameless.features.base

import kotlin.reflect.KMutableProperty0
import kotlin.reflect.jvm.isAccessible


// TODO Set ordinal by call order
class ParameterHierarchy(private val hierarchyParent: AbstractDefaultFeature<*>) {

    fun KMutableProperty0<*>.getDelegateParameter(): FeatureParameter<*> {
        isAccessible = true
        return getDelegate() as FeatureParameter<*>
    }

    operator fun KMutableProperty0<*>.unaryPlus() {
        +getDelegateParameter()
    }

    operator fun FeatureParameter<*>.unaryPlus() {
        hierarchyParent.parameters[key] = this
        parent = hierarchyParent
    }

    operator fun KMutableProperty0<*>.unaryMinus() {
        -getDelegateParameter()
    }

    operator fun FeatureParameter<*>.unaryMinus() {
        hierarchyParent.parameters.remove(key, this)
        parent = null
    }

    inline operator fun KMutableProperty0<*>.invoke(action: ParameterHierarchy.() -> Unit) {
        getDelegateParameter()(action)
    }

    inline operator fun FeatureParameter<*>.invoke(action: ParameterHierarchy.() -> Unit) {
        +this
        ParameterHierarchy(this).action()
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
        ParameterHierarchy(this).action()
    }
}

inline fun BaseFeature<*>.executeHierarchy(action: ParameterHierarchy.() -> Unit) {
    ParameterHierarchy(this).action()
}