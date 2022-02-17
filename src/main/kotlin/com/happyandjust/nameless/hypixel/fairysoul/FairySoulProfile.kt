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

package com.happyandjust.nameless.hypixel.fairysoul

import com.happyandjust.nameless.config.ConfigHandler
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FairySoulProfile(
    val name: String,
    @SerialName("fairySouls") val foundFairySouls: MutableMap<String, MutableList<FairySoul>>
) {

    fun addFoundFairySoul(fairySoul: FairySoul) {
        if (fairySoul.island == "dungeon") return

        foundFairySouls.getOrPut(fairySoul.island) { arrayListOf() }.add(fairySoul)

        ConfigHandler.write("profiles", name, this)
    }
}