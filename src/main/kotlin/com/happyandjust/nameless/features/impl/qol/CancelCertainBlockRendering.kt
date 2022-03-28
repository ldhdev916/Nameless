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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.BlockSerializer
import com.happyandjust.nameless.dsl.displayName
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import kotlinx.serialization.builtins.ListSerializer
import net.minecraft.block.Block

object CancelCertainBlockRendering : SimpleFeature("cancelBlockRendering", "Cancel Certain Block Rendering") {

    init {
        hierarchy { +::blocks }
    }

    @JvmStatic
    var blocks by parameter(emptyList(), serializer = ListSerializer(BlockSerializer)) {
        matchKeyCategory()
        key = "blocks"
        title = "Blocks"

        settings {
            listSerializer { it.displayName }
            allValueList = {
                Block.blockRegistry.sortedWith(compareBy({ it !in value }, { it.displayName }))
            }
        }
    }
}