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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import com.happyandjust.nameless.mixinhooks.EntityHook

object DamageIndicator : SimpleFeature(
    "damageIndicator",
    "Damage Indicator",
    "Transform damage into K or M or B"
) {

    init {
        hierarchy {
            +::type

            +::precision
        }
    }

    var type by parameter(DamageIndicateType.SMART) {
        key = "type"
        title = "Damage Indicate Type"

        onValueChange {
            EntityHook.transformedDamageCache.clear()
        }
        settings { autoFillEnum() }
    }

    var precision by parameter(1) {
        key = "precision"
        title = "Floating Point Precision"

        settings {
            maxValueInt = 7
        }

        onValueChange {
            EntityHook.transformedDamageCache.clear()
        }
    }
}