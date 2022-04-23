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

package com.happyandjust.nameless.hypixel.skyblock.experimentation

import com.happyandjust.nameless.dsl.TempEventListener

interface ExperimentationGame : TempEventListener

interface ExperimentationGameCreator {
    fun createImpl(): ExperimentationGame

    val chestDisplayName: String
}

@Suppress("unused")
enum class ExperimentationType : ExperimentationGameCreator {
    Chronomatron {
        override fun createImpl() = Chronomatron()

        override val chestDisplayName = "Chronomatron"
    },
    Superpairs {
        override fun createImpl() = Superpairs()

        override val chestDisplayName = "Superpairs"
    },
    UltraSequencer {
        override fun createImpl() = UltraSequencer()

        override val chestDisplayName = "Ultrasequencer"
    }
}