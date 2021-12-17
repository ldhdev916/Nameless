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

package com.happyandjust.nameless.dsl

inline fun <T, A : Any, B : Any> instanceOrNull(param1: A?, param2: B?, constructor: (A, B) -> T): T? {
    return constructor(param1 ?: return null, param2 ?: return null)
}

inline fun <T, A : Any, B : Any, C : Any> instanceOrNull(
    param1: A?,
    param2: B?,
    param3: C?,
    constructor: (A, B, C) -> T
): T? {
    return constructor(param1 ?: return null, param2 ?: return null, param3 ?: return null)
}