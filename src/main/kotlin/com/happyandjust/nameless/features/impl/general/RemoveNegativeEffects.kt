/*
 *var enabledPotionTypes
get() = this.enabledPotionTypes__273e5a27_8e4c_4c6a_af77_ac19194e43a1.value
set(value) {
        this.enabledPotionTypes__273e5a27_8e4c_4c6a_af77_ac19194e43a1.value = value
    } Nameless - 1.8.9 Hypixel Quality Of Life Mod
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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.autoFillEnum
import com.happyandjust.nameless.features.base.listParameter
import com.happyandjust.nameless.features.enabledPotionTypes

object RemoveNegativeEffects : SimpleFeature(
    "removeNegativeEffects",
    "Remove Negative Effects",
    "Support Blindness, Nausea"
) {

    @JvmStatic
    var enabledPotionTypesJVM
        get() = enabledPotionTypes
        set(value) {
            enabledPotionTypes = value
        }

    @JvmStatic
    val enabledJVM
        get() = enabled

    init {
        listParameter(PotionType.entries) {
            matchKeyCategory()
            key = "enabledPotionTypes"
            title = "Potion Types"

            autoFillEnum {
                val name = it.name
                "${name[0]}${name.drop(1).lowercase()}"
            }
        }
    }

    enum class PotionType {
        BLINDNESS, NAUSEA
    }
}