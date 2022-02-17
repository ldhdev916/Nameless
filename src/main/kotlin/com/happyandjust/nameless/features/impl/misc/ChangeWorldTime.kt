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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.autoFillEnum
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.features.time
import com.happyandjust.nameless.features.timeFormat

object ChangeWorldTime : SimpleFeature(
    "changeWorldTime",
    "Change World Time",
    "This changes sky color so requires Change Sky Color to be turned off"
) {

    @JvmStatic
    var timeJVM
        get() = time
        set(value) {
            time = value
        }

    init {
        parameter(0) {
            matchKeyCategory()
            key = "time"
            title = "Precise World Time"

            settings {
                maxValueInt = 23999
            }

            onValueChange { time ->
                timeFormat = WorldTimeFormat.values().single { time in it.ordinal * 1000 until (it.ordinal + 1) * 1000 }
            }
        }

        parameter(WorldTimeFormat.AM12) {
            matchKeyCategory()
            key = "timeFormat"
            title = "World Time Formatted"

            autoFillEnum { it.prettyName }

            onValueChange {
                time = it.ordinal * 1000
            }
        }
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