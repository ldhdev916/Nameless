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

import com.happyandjust.nameless.core.property.KPropertyBackedPropertyValue
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.base.BaseFeature
import com.happyandjust.nameless.features.base.IRelocateAble
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.relocate.RelocateGui

object OpenRelocateGui : BaseFeature<() -> Unit>("relocateGui", "Relocate Gui", "Edit gui position, scale") {

    private var action = {
        mc.displayGuiScreen(
            RelocateGui(
                allDefaultFeatures.filterIsInstance<IRelocateAble>()
                    .filter { it.shouldDisplayInRelocateGui() })
        )
    }

    override val propertyValue = KPropertyBackedPropertyValue(::action)
    override var componentType: ComponentType? = ComponentType.BUTTON

    init {
        settings {
            placeHolder = "Relocate"
        }
    }
}