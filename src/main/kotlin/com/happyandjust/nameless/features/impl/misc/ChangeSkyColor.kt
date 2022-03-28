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

import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import net.minecraft.util.Vec3
import java.awt.Color

object ChangeSkyColor : SimpleFeature("changeSkyColor", "Change Sky Color") {

    init {
        hierarchy {
            +::skyColor
        }
    }

    private var skyColor by parameter(Color.black.toChromaColor()) {
        key = "skyColor"
        title = "Sky Color"
        desc = "Alpha is not allowed"
    }

    @JvmStatic
    val convert
        get() = Vec3(skyColor.red / 255.0, skyColor.green / 255.0, skyColor.blue / 255.0)
}