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

package com.happyandjust.nameless.core.value

import com.happyandjust.nameless.dsl.withAlpha
import java.awt.Color

/**
 * Only use [java.awt.Color.getRGB]
 */
class ChromaColor(val originRGB: Int) : Color(originRGB, true) {

    /**
     * Due to lack of my programming skills, Chroma Speed is Immutable
     */
    val chromaSpeed = 2000
    var chromaEnabled = false

    override fun getRGB(): Int {
        return if (!chromaEnabled) super.getRGB() else HSBtoRGB(
            (System.currentTimeMillis() % chromaSpeed) / chromaSpeed.toFloat(),
            1f,
            0.8f
        ).withAlpha(super.getRGB() shr 24 and 255)
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other) && other is ChromaColor && other.chromaEnabled == chromaEnabled
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + originRGB
        result = 31 * result + chromaSpeed
        result = 31 * result + chromaEnabled.hashCode()
        return result
    }
}

fun Color.toChromaColor(chromaEnabled: Boolean = false) = ChromaColor(rgb).also {
    it.chromaEnabled = chromaEnabled
}