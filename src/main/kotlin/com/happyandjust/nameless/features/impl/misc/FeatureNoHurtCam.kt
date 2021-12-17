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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import kotlin.reflect.KMutableProperty0

object FeatureNoHurtCam : SimpleFeature(
    Category.MISCELLANEOUS,
    "nohurtcam",
    "HurtCam Adjuster",
    "Adjust the hurt animation when being hit(default 14)"
) {

    var hurtCamModifier = 14

    override fun getComponentType(): ComponentType {
        return ComponentType.SLIDER
    }

    override fun toPropertyData(): PropertyData<*> {
        return super.toPropertyData().apply {
            minValue = 0.0
            maxValue = 50.0
        }
    }

    override fun getProperty(): KMutableProperty0<*> {
        return ::hurtCamModifier
    }
}