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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.MOD_ID
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer

object RemoveCertainModID : SimpleFeature(
    "removeModId",
    "Remove Certain Mod ID Sent to Server",
    initialEnabled = true
) {

    init {
        val mods = Loader::class.java.getDeclaredField("mods")
            .apply { isAccessible = true }[Loader.instance()] as List<*>
        for (mod in mods.filterIsInstance<ModContainer>()) {
            parameter(mod.modId == MOD_ID) {
                matchKeyCategory()
                key = mod.modId
                title = "${mod.name} ${mod.version}"
                desc = "Source File: ${mod.source.absolutePath}"
            }
        }
    }
}