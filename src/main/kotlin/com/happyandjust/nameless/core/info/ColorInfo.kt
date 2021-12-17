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

package com.happyandjust.nameless.core.info

data class ColorInfo(val color: Int, val priority: ColorPriority) {

    operator fun compareTo(other: ColorInfo?) = priority.compareTo(other?.priority)

    enum class ColorPriority(val number: Int) {
        HIGHEST(5), HIGH(4), NORMAL(3), LOW(2), LOWEST(1);

        operator fun compareTo(other: ColorPriority?) = number.compareTo(other?.number ?: -1)
    }
}

fun ColorInfo?.checkAndReplace(other: ColorInfo): ColorInfo {
    this ?: return other
    return if (other >= this) other else this
}

fun ColorInfo?.checkAndReplace(color: Int, priority: ColorInfo.ColorPriority) =
    checkAndReplace(ColorInfo(color, priority))