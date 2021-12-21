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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.serialization.converters.getEnumConverter

object FeatureChangeWorldTime : SimpleFeature(
    Category.MISCELLANEOUS,
    "changeworldtime",
    "Change World Time",
    "This changes sky color so requires Change Sky Color to be turned off"
) {

    var worldTime: Int by FeatureParameter(1, "worldtime", "time", "Precise World Time", "", 0, CInt).apply {
        minValue = 0.0
        maxValue = 23999.0

        onValueChange = { time ->
            worldTimeFormat = WorldTimeFormat.values().first { time in it.ordinal * 1000 until (it.ordinal + 1) * 1000 }
        }
    }
    private var worldTimeFormat by FeatureParameter(
        0,
        "worldtime",
        "timeformat",
        "World Time Formatted",
        "",
        WorldTimeFormat.AM12,
        getEnumConverter()
    ).apply {
        onValueChange = {
            worldTime = it.ordinal * 1000
        }
        enumName = { (it as WorldTimeFormat).prettyName }
    }

    enum class WorldTimeFormat(val prettyName: String) {
        AM12("AM 12:00"),
        AM1("AM 1:00"),
        AM2("AM 2:00"),
        AM3("AM 3:00"),
        AM4("AM 4:00"),
        AM5("AM 5:00"),
        AM6("AM 6:00"),
        AM7("AM 7:00"),
        AM8("AM 8:00"),
        AM9("AM 9:00"),
        AM10("AM 10:00"),
        AM11("AM 11:00"),
        PM12("PM 12:00"),
        PM1("PM 1:00"),
        PM2("PM 2:00"),
        PM3("PM 3:00"),
        PM4("PM 4:00"),
        PM5("PM 5:00"),
        PM6("PM 6:00"),
        PM7("PM 7:00"),
        PM8("PM 8:00"),
        PM9("PM 9:00"),
        PM10("PM 10:00"),
        PM11("PM 11:00")
    }
}