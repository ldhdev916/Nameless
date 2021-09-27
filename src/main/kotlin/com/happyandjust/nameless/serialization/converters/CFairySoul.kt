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

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.serialization.Converter

class CFairySoul : Converter<FairySoul> {
    override fun serialize(t: FairySoul): JsonElement {
        return JsonObject().apply {
            addProperty("x", t.x)
            addProperty("y", t.y)
            addProperty("z", t.z)
            addProperty("island", t.island)
        }
    }

    override fun deserialize(jsonElement: JsonElement): FairySoul {
        val jsonObject = jsonElement.asJsonObject

        return FairySoul(
            jsonObject["x"].asInt,
            jsonObject["y"].asInt,
            jsonObject["z"].asInt,
            jsonObject["island"].asString
        )
    }
}
