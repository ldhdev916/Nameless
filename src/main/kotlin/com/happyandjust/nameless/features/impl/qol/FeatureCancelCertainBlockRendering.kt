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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.dsl.displayName
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CBoolean
import net.minecraft.block.Block

object FeatureCancelCertainBlockRendering :
    SimpleFeature(Category.QOL, "cancelblockrendering", "Cancel Certain Block Rendering") {

    init {
        for (block in Block.blockRegistry) {
            parameters[block.registryName] = FeatureParameter(
                1,
                "cancelblock",
                block.registryName,
                block.displayName,
                "",
                false,
                CBoolean
            ).apply {
                if (value) ordinal = 0
                onValueChange = {
                    mc.renderGlobal.loadRenderers()

                    ordinal = if (it) 0 else 1
                }
            }
        }
    }
}