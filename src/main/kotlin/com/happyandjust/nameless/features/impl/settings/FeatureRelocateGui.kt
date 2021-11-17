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

package com.happyandjust.nameless.features.impl.settings

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.IRelocateAble
import com.happyandjust.nameless.features.SettingFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import com.happyandjust.nameless.gui.relocate.RelocateGui
import kotlin.reflect.KMutableProperty0

object FeatureRelocateGui : SettingFeature("relocategui", "Relocate Gui", "Edit gui position, scale") {

    private var action = {
        mc.displayGuiScreen(
            RelocateGui(
                allDefaultFeatures.filterIsInstance<IRelocateAble>()
                    .filter { it.shouldDisplayInRelocateGui() })
        )
    }

    override fun getProperty(): KMutableProperty0<*> {
        return ::action
    }

    override fun getComponentType(): ComponentType {
        return ComponentType.BUTTON
    }

    override fun toPropertyData(): PropertyData<*> {
        return super.toPropertyData().also { it.placeHolder = "Relocate" }
    }
}