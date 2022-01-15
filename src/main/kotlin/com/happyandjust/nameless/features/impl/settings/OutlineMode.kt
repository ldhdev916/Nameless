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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.enums.OutlineMode
import com.happyandjust.nameless.features.base.SettingFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import kotlin.reflect.KMutableProperty0

object OutlineMode : SettingFeature("outlinemode", "Outline Mode", "BOX, OUTLINE") {

    override fun getProperty(): KMutableProperty0<*> = Nameless::selectedOutlineMode

    override fun getComponentType() = ComponentType.SELECTOR

    override fun toPropertyData(): PropertyData<out Any?> {
        return super.toPropertyData().also {
            it.allEnumList = OutlineMode.values().toList()
        }
    }
}