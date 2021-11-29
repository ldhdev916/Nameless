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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CBoolean
import net.minecraft.block.Block
import net.minecraft.item.ItemStack

object FeatureCancelCertainBlockRendering :
    SimpleFeature(Category.QOL, "cancelblockrendering", "Cancel Certain Block Rendering") {

    init {
        for (block in Block.blockRegistry) {
            val displayName =
                runCatching { ItemStack(block).displayName }.getOrDefault(block.registryName.split(":")[1])

            parameters[block.registryName] = FeatureParameter(
                0,
                "cancelblock",
                block.registryName,
                displayName,
                "",
                false,
                CBoolean
            ).apply {
                onValueChange = { mc.renderGlobal.loadRenderers() }
            }
        }
    }
}