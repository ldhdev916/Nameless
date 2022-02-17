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

package com.happyandjust.nameless.core

data class TickTimer(private val targetTick: Int) {

    private var currentTick = 0

    fun reset() = apply {
        currentTick = 0
    }

    fun update() = apply {
        currentTick = (currentTick + 1) % targetTick
    }

    fun check() = currentTick == 0

    companion object {
        fun withSecond(second: Number) = TickTimer((second.toDouble() * 20).toInt())
    }
}