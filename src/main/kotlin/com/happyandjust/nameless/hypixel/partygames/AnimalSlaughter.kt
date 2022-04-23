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

package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper

class AnimalSlaughter : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = PartyGamesHelper.animal

    override fun registerEventListeners() {
        on<OutlineRenderEvent>().filter { "-50%" in entity.displayName.unformattedText }.addSubscribe {
            colorInfo = ColorInfo(outlineColor, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : PartyMiniGamesCreator {
        override fun createImpl() = AnimalSlaughter()

        override val scoreboardIdentifier = "Animal Slaughter"

        private val outlineColor
            get() = PartyGamesHelper.animalColor.rgb
    }
}