package com.happyandjust.nameless.core.property

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.features.PropertySetting
import com.happyandjust.nameless.gui.feature.ComponentType
import kotlin.reflect.KMutableProperty0


data class PropertyData(
    val propertyValue: PropertyValue,
    val title: String,
    val desc: String,
    val componentType: ComponentType?,
    val propertySetting: PropertySetting,
    val settings: List<PropertyData> = emptyList()
)

interface PropertyValue {
    fun getValue(): Any

    fun setValue(value: Any)
}

data class KPropertyBackedPropertyValue<T : Any>(private val property: KMutableProperty0<T>) : PropertyValue {
    override fun getValue(): Any {
        return property()
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any) {
        property.set(value as T)
    }
}

data class ConfigBackedPropertyValue<T : Any>(private val config: ConfigValue<T>) : PropertyValue {
    override fun getValue(): Any {
        return config.value
    }

    @Suppress("UNCHECKED_CAST")
    override fun setValue(value: Any) {
        config.value = value as T
    }
}