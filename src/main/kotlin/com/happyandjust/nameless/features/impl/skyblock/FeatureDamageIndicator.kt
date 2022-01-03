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

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import com.happyandjust.nameless.mixinhooks.EntityHook
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.serialization.converters.getEnumConverter

object FeatureDamageIndicator : SimpleFeature(
    Category.SKYBLOCK,
    "damageindicator",
    "Damage Indicator",
    "Transform damage into K or M or B"
) {

    var type by FeatureParameter(
        0,
        "damageindicator",
        "type",
        "Damage Indicate Type",
        "K, M, B, SMART",
        DamageIndicateType.SMART,
        getEnumConverter()
    ).apply {
        onValueChange = {
            EntityHook.transformedDamageCache.clear()
        }
    }

    var precision by FeatureParameter(
        0,
        "damageindicator",
        "precision",
        "Precision",
        "",
        1,
        CInt
    ).apply {
        minValue = 0.0
        maxValue = 7.0

        onValueChange = {
            EntityHook.transformedDamageCache.clear()
        }
    }
}