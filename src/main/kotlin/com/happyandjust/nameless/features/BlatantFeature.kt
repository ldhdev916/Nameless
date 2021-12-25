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

package com.happyandjust.nameless.features

import com.happyandjust.nameless.dsl.cancel
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.gui.blatant.RequestUseBlatantFeatureGui
import gg.essential.api.utils.GuiUtil
import net.minecraftforge.fml.common.eventhandler.EventPriority

abstract class BlatantFeature(
    category: Category,
    key: String,
    title: String,
    desc: String = "",
    enabled_: Boolean = false
) : SimpleFeature(category, key, title, desc, enabled_) {

    abstract var useAccepted: Boolean
    abstract val reasonForBlatant: String

    init {
        on<FeatureStateChangeEvent.Pre>().apply { priority = EventPriority.HIGHEST }
            .filter { feature == this@BlatantFeature && enabledAfter }.subscribe {
                if (!useAccepted) {
                    cancel()
                    GuiUtil.open(RequestUseBlatantFeatureGui(this@BlatantFeature))
                }
            }
    }
}