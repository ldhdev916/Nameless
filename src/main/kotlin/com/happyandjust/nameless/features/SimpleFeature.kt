/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2021 HappyAndJust
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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.processor.Processor
import net.minecraftforge.common.MinecraftForge
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class SimpleFeature(
    val category: Category,
    val key: String,
    val title: String,
    val desc: String = "",
    enabled_: Boolean = false
) {

    var inCategory = ""
    private val enabledConfig = ConfigValue.BooleanConfigValue("features", key, enabled_)
    protected val threadPool: ExecutorService = Executors.newFixedThreadPool(2)
    val parameters = hashMapOf<String, FeatureParameter<*>>()
    val processors = hashMapOf<Processor, () -> Boolean>()

    fun <T> getParameter(key: String) = parameters[key] as FeatureParameter<T>

    fun <T> getParameterValue(key: String) = getParameter<T>(key).value

    var enabled: Boolean
        get() = enabledConfig.value
        set(value) {
            val event = FeatureStateChangeEvent.Pre(this, value)
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                enabledConfig.value = event.enabledAfter

                MinecraftForge.EVENT_BUS.post(FeatureStateChangeEvent.Post(this, enabledConfig.value))
            }
        }

    fun invertEnableState() {
        enabled = !enabled
    }

}
