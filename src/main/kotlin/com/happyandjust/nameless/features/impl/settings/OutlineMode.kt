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

package com.happyandjust.nameless.features.impl.settings

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.enums.OutlineMode
import com.happyandjust.nameless.features.base.BaseFeature
import com.happyandjust.nameless.features.base.autoFillEnum
import com.happyandjust.nameless.gui.feature.ComponentType

object OutlineMode : BaseFeature<OutlineMode, Any>("outlineMode", "Outline Mode", "BOX, OUTLINE") {

    override val property = Nameless::selectedOutlineMode
    override var componentType: ComponentType? = ComponentType.SELECTOR

    init {
        autoFillEnum()
    }
}