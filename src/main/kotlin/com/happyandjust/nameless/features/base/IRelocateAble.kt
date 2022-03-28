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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.config.ConfigValue.Companion.configValue
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.gui.relocate.RelocateGui
import gg.essential.elementa.UIComponent
import kotlinx.serialization.KSerializer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

interface IRelocateAble {

    /**
     * Make sure you use delegate to save to config
     */
    var overlayPoint: Overlay

    fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent

    fun getWheelSensitive() = 13

    fun getDisplayName(): String

    fun shouldDisplayInRelocateGui(): Boolean

    fun renderOverlay0(partialTicks: Float)
}

abstract class OverlayFeature(
    key: String,
    title: String,
    desc: String = "",
    enabled_: Boolean = false
) : SimpleFeature(key, title, desc, enabled_), IRelocateAble {
    override fun getDisplayName() = title

    init {
        on<SpecialOverlayEvent>().filter { mc.currentScreen !is RelocateGui }.subscribe { renderOverlay0(partialTicks) }
    }
}

class OverlayParameter<T : Any>(defaultValue: T, serializer: KSerializer<T>) :
    FeatureParameter<T>(defaultValue, serializer), IRelocateAble {

    private lateinit var configDelegate: ConfigValue<Overlay>
    private lateinit var relocateComponent: (RelocateComponent) -> UIComponent
    var wheel = 13
    private var displayName = { title }
    private lateinit var aShouldDisplayInRelocateGui: () -> Boolean
    private lateinit var render: (Float) -> Unit

    fun config(category: String, key: String, defaultValue: Overlay) {
        configDelegate = configValue(category, key, defaultValue)
    }

    fun component(getter: RelocateComponent.() -> UIComponent) {
        relocateComponent = getter
    }

    fun name(getter: () -> String) {
        displayName = getter
    }

    fun shouldDisplay(getter: () -> Boolean) {
        aShouldDisplayInRelocateGui = getter
    }

    fun render(operation: (partialTicks: Float) -> Unit) {
        render = operation
    }

    init {
        on<SpecialOverlayEvent>().filter { mc.currentScreen !is RelocateGui }.subscribe { renderOverlay0(partialTicks) }
    }

    override var overlayPoint by object : ReadWriteProperty<OverlayParameter<T>, Overlay> {
        override fun getValue(thisRef: OverlayParameter<T>, property: KProperty<*>) = configDelegate.value

        override fun setValue(thisRef: OverlayParameter<T>, property: KProperty<*>, value: Overlay) {
            configDelegate.value = value
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent) = this.relocateComponent(relocateComponent)

    override fun getDisplayName() = displayName()

    override fun shouldDisplayInRelocateGui() = aShouldDisplayInRelocateGui()

    override fun renderOverlay0(partialTicks: Float) = render(partialTicks)

    override fun getWheelSensitive() = wheel
}