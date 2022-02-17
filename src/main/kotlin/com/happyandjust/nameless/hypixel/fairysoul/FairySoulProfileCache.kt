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

import com.happyandjust.nameless.config.configMap
import com.happyandjust.nameless.config.configValue
import com.happyandjust.nameless.dsl.mc

object FairySoulProfileCache {

    private val defaultProfile = FairySoulProfile("default", hashMapOf())
    private val generatedFairySoulProfiles = configMap<FairySoulProfile>("profiles")
    var currentlyLoadedProfile by configValue(
        "fairysoul",
        "currentprofile",
        defaultProfile
    )

    fun changeToProfileAndIfNotExistThenCreate(profileName: String) {
        val uuid = mc.session.playerID

        val name = "$uuid-$profileName"
        currentlyLoadedProfile = generatedFairySoulProfiles.getOrPut(name) { FairySoulProfile(name, hashMapOf()) }
    }
}