package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.dsl.TempEventListener
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.ParameterHierarchy
import com.happyandjust.nameless.features.base.parameter
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