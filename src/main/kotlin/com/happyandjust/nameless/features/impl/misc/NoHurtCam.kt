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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.config.configValue
import com.happyandjust.nameless.features.base.BaseFeature
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.ComponentType

object NoHurtCam : BaseFeature<Int, Any>(
    "noHurtCam",
    "HurtCam Adjuster",
    "Adjust the hurt animation when being hit(default 14)"
) {

    @JvmStatic
    var hurtCamModifier by configValue("hurtCam", "value", 14)
    override var componentType: ComponentType? = ComponentType.SLIDER
    override val property = ::hurtCamModifier

    init {
        settings {
            minValueInt = 0
            maxValueInt = 50
        }
    }
}