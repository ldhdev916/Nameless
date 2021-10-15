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

import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.TypeRegistry
import net.minecraft.block.Block
import net.minecraft.item.ItemStack

class FeatureCancelCertainBlockRendering :
    SimpleFeature(Category.QOL, "cancelblockrendering", "Cancel Certain Block Rendering") {

    init {
        val cBoolean = TypeRegistry.getConverterByClass(Boolean::class)

        for (block in Block.blockRegistry) {
            val displayName = try {
                ItemStack(block).displayName
            } catch (e: NullPointerException) {
                block.registryName.split(":")[1]
            }

            parameters[block.registryName] = FeatureParameter(
                0,
                "cancelblock",
                block.registryName,
                displayName,
                "",
                false,
                cBoolean
            ).also {
                it.onToggleClick = {
                    if (enabled) {
                        mc.renderGlobal.loadRenderers()
                    }
                }
            }
        }
    }
}