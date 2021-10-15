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

package com.happyandjust.nameless.serialization

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.core.OutlineMode
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.fairysoul.FairySoulProfile
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import com.happyandjust.nameless.serialization.converters.*
import com.happyandjust.nameless.textureoverlay.Overlay
import kotlin.reflect.KClass

object TypeRegistry {

    private val registeredConverters = hashMapOf(
        Int::class to CInt,
        Double::class to CDouble,
        Boolean::class to CBoolean,
        String::class to CString,
        Float::class to CFloat,
        FairySoul::class to CFairySoul,
        ChromaColor::class to CChromaColor,
        Overlay::class to COverlay,
        FairySoulProfile::class to CFairySoulProfile,
        DamageIndicateType::class to CDamageIndicateType,
        OutlineMode::class to COutlineMode
    )

    fun <T : Any> getConverterByClass(clazz: KClass<T>) = registeredConverters[clazz] as Converter<T>
}
