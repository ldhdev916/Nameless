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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CString
import com.happyandjust.nameless.utils.SkyblockUtils

object FeatureChangeItemName :
    SimpleFeature(Category.SKYBLOCK, "changeitemname", "Change Item Name", "Auto converts & into §") {

    fun changeItemName(skyblockId: String, originName: String): String {
        val parameter = getParameter<Boolean>(skyblockId.lowercase())

        val overwrite = parameter.getParameterValue<String>("overwrite").replace("&", "§")
        val prefix = parameter.getParameterValue<String>("prefix").replace("&", "§")
        val suffix = parameter.getParameterValue<String>("suffix").replace("&", "§")
        val replace = parameter.getParameterValue<String>("replace").replace("&", "§")

        if (overwrite.isNotBlank()) {
            return overwrite
        }

        var processedName = originName

        if (replace.isNotBlank()) {
            for (replacement in replace.split(",")) {
                val pair = convertToReplacementPair(replacement)

                processedName = processedName.replace(pair.first, pair.second)
            }
        }

        if (prefix.isNotBlank()) {
            processedName = prefix + processedName
        }

        if (suffix.isNotBlank()) {
            processedName += suffix
        }

        return processedName
    }

    private fun convertToReplacementPair(s: String) = with(s.split("|")) {
        if (size < 2) Pair("", "") else Pair(this[0], this[1])
    }

    fun isChangedItem(skyblockId: String) = parameters[skyblockId.lowercase()]?.value as? Boolean == true

    fun updateItemData() {
        for (skyBlockItem in SkyblockUtils.allItems.values) {

            val id = skyBlockItem.id.lowercase()

            parameters[id] = FeatureParameter(
                0,
                "changeitemname",
                id,
                skyBlockItem.name,
                "SkyBlock ID: ${skyBlockItem.id}",
                false,
                CBoolean
            ).also {
                it.parameters["overwrite"] = FeatureParameter(
                    0,
                    "changeitemname",
                    "${id}_overwrite",
                    "Custom Name of ${skyBlockItem.name}",
                    "This will ignore all prefix, suffix, replaces and just overwrites whole item's name, remain empty to disable",
                    "",
                    CString
                )

                it.parameters["prefix"] = FeatureParameter(
                    0,
                    "changeitemname",
                    "${id}_prefix",
                    "Custom Prefix of ${skyBlockItem.name}",
                    "Remain empty to disable",
                    "",
                    CString
                )

                it.parameters["suffix"] = FeatureParameter(
                    0,
                    "changeitemname",
                    "${id}_suffix",
                    "Custom Suffix of ${skyBlockItem.name}",
                    "Remain empty to disable",
                    "",
                    CString
                )

                it.parameters["replace"] = FeatureParameter(
                    0,
                    "changeitemname",
                    "${id}_replace",
                    "Custom Replacement of ${skyBlockItem.name}",
                    "This will replace certain words of item's displayName Usage: from|to and split with comma\nExample:Aspect|NoAspect,Hyperion|Hype\nremain empty to disable",
                    "",
                    CString
                )
            }
        }
    }
}