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

package com.happyandjust.nameless.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.core.NameHistory
import com.happyandjust.nameless.core.Request

object APIUtils {

    private val gson = Gson()

    fun getUUIDFromUsername(username: String): String =
        JSONHandler(Request.get("https://api.mojang.com/users/profiles/minecraft/$username")).read(JsonObject())["id"].asString

    fun getNameHistoryFromUUID(uuid: String): List<NameHistory> {
        val json = JSONHandler(Request.get("https://api.mojang.com/user/profiles/$uuid/names")).read(JsonArray())

        return json.map { gson.fromJson(it, NameHistory::class.java) }
    }
}