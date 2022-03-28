package com.happyandjust.nameless.features.base

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