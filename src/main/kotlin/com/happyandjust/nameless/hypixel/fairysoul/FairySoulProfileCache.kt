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

package com.happyandjust.nameless.hypixel.fairysoul

import com.happyandjust.nameless.config.ConfigMap
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.serialization.converters.CFairySoulProfile

object FairySoulProfileCache {

    private val defaultProfile = FairySoulProfile("default", hashMapOf())
    private val generatedFairySoulProfiles = ConfigMap(
        "profiles",
        defaultProfile,
        CFairySoulProfile
    )
    private val currentlyLoadedProfileConfig =
        ConfigValue(
            "fairysoul",
            "currentprofile",
            defaultProfile,
            CFairySoulProfile
        )
    var currentlyLoadedProfile: FairySoulProfile = currentlyLoadedProfileConfig.value
        set(value) {
            field = value
            currentlyLoadedProfileConfig.value = value
        }

    fun changeToProfileAndIfNotExistThenCreate(profileName: String) {
        val uuid = mc.session.playerID

        val name = "$uuid-$profileName"

        currentlyLoadedProfile = generatedFairySoulProfiles[name] ?: FairySoulProfile(name, hashMapOf()).also {
            generatedFairySoulProfiles[name] = it
        }
    }
}