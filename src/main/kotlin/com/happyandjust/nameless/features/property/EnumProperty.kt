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

package com.happyandjust.nameless.features.property

import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.gui.Rectangle
import com.happyandjust.nameless.gui.elements.EEnumSelectPanel

class EnumProperty(featureParameter: FeatureParameter<Enum<*>>) : Property<Enum<*>>(
    featureParameter, EEnumSelectPanel(
        Rectangle.fromWidthHeight(0, 0, 50, 14),
        featureParameter.value,
        featureParameter.allEnumList
    ).also {
        it.onCurrentEnumChange = { enum -> featureParameter.value = enum }
    }
)