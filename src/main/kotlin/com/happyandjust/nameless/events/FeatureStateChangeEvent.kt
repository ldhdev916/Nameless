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

package com.happyandjust.nameless.events

import com.happyandjust.nameless.features.SimpleFeature
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

abstract class FeatureStateChangeEvent(val feature: SimpleFeature, var enabledAfter: Boolean) : Event() {

    @Cancelable
    class Pre(feature: SimpleFeature, enabledAfter: Boolean) : FeatureStateChangeEvent(feature, enabledAfter)

    class Post(feature: SimpleFeature, enabledAfter: Boolean) : FeatureStateChangeEvent(feature, enabledAfter)
}
