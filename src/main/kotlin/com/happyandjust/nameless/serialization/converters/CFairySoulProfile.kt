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

package com.happyandjust.nameless.serialization.converters

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.fairysoul.FairySoulProfile
import com.happyandjust.nameless.serialization.Converter

object CFairySoulProfile : Converter<FairySoulProfile> {

    override fun serialize(t: FairySoulProfile): JsonElement {

        val jsonObject = JsonObject()

        jsonObject.addProperty("name", t.name)

        val islands = JsonObject()

        for ((island, foundFairySouls) in t.foundFairySouls) {
            val array = JsonArray()

            for (fairySoul in foundFairySouls) {
                array.add(CFairySoul.serialize(fairySoul))
            }

            islands.add(island, array)
        }

        jsonObject.add("fairySouls", islands)
        return jsonObject
    }

    override fun deserialize(jsonElement: JsonElement): FairySoulProfile {
        val jsonObject = jsonElement.asJsonObject

        val name = jsonObject["name"].asString
        val fairySoulsByIsland = jsonObject["fairySouls"].asJsonObject

        val fairySoulMap = hashMapOf<String, List<FairySoul>>()

        for ((island, foundFairySouls) in fairySoulsByIsland.entrySet()) {
            foundFairySouls as JsonArray

            val fairySouls = arrayListOf<FairySoul>()

            for (fairySoul in foundFairySouls) {
                fairySouls.add(CFairySoul.deserialize(fairySoul))
            }

            fairySoulMap[island] = fairySouls
        }

        return FairySoulProfile(name, fairySoulMap)
    }
}