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

import com.happyandjust.nameless.config.ConfigValue.Companion.configValue
import com.happyandjust.nameless.core.property.KPropertyBackedPropertyValue
import com.happyandjust.nameless.core.property.PropertyValue
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.gui.feature.ComponentType
import net.minecraftforge.common.MinecraftForge

abstract class SimpleFeature(
    key: String,
    title: String,
    desc: String = "",
    enabled: Boolean = false
) : BaseFeature<Boolean>(key, title, desc) {

    private val enabledConfig = configValue("features", key, enabled)
    override var componentType: ComponentType? = ComponentType.SWITCH
    override val propertyValue: PropertyValue by lazy { KPropertyBackedPropertyValue(this::enabled) }

    var enabled = enabledConfig.value
        set(value) {
            val event = FeatureStateChangeEvent.Pre(this, value)
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                field = event.enabledAfter
                enabledConfig.value = event.enabledAfter

                MinecraftForge.EVENT_BUS.post(FeatureStateChangeEvent.Post(this, enabledConfig.value))
            }
        }
}

abstract class BaseFeature<T : Any>(
    key: String,
    title: String,
    desc: String = ""
) : AbstractDefaultFeature<T>() {

    init {
        this.key = key
        this.title = title
        this.desc = desc
    }

    lateinit var category: Category
    val categoryInitialized
        get() = ::category.isInitialized
}