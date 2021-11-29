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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.processor.Processor
import net.minecraft.entity.Entity

object AnimalSlaughterProcessor : Processor(), StencilListener {
    var entityColor = { -1 }

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        return if (entity.displayName.unformattedText.contains("-50%")) ColorInfo(
            entityColor(),
            ColorInfo.ColorPriority.HIGH
        ) else null
    }

}