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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.features.rpg16_color
import com.happyandjust.nameless.processor.Processor
import net.minecraft.entity.player.EntityPlayer

object RPG16Processor : Processor() {

    override val filter = PartyGamesHelper.getFilter(this)

    init {
        request<OutlineRenderEvent>().filter { entity is EntityPlayer && entity.health <= 2.1f }.subscribe {
            colorInfo = ColorInfo(PartyGamesHelper.rpg16_color.rgb, ColorInfo.ColorPriority.HIGH)
        }
    }
}