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

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.command
import com.happyandjust.nameless.features.delay
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.features.waitForGG
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

object AutoRequeue : SimpleFeature(
    "autoRequeue",
    "Auto Requeue",
    "Auto send /play command you set after game end\n§cMake sure to set hypixel language as English"
) {

    init {
        parameter("/play ranked_normal") {
            matchKeyCategory()
            key = "command"
            title = "Play Command"
            desc = "The play command mod will send after game end"
        }
        parameter(0) {
            matchKeyCategory()
            key = "delay"
            title = "Delay"
            desc = "Send delay in seconds"

            settings {
                ordinal = 1
                maxValueInt = 5
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "waitForGG"
            title = "Wait GG"
            desc = "Wait until AutoGG send gg message then send command"

            settings {
                ordinal = 2
            }
        }
    }

    var isAutoGGLoaded by Delegates.observable(false) { _, _, newValue ->
        if (!newValue) parameters.remove("waitForGG")
    }

    private val triggers = run {
        val jsonObject =
            Json.decodeFromString<JsonObject>("https://static.sk1er.club/autogg/regex_triggers_3.json".fetch())
        jsonObject["servers"]!!.jsonArray[0].jsonObject["triggers"]!!.jsonArray.mapNotNull {
            if (it.jsonObject["type"]?.int == 0) it.jsonObject["pattern"]!!.string.toPattern() else null
        }
    }

    private val ggPattern = "\\w: gg".toPattern()
    private val karmaPattern = "\\+\\d+ Karma!".toPattern()

    private var shouldDetectGG = false
    private var sentGG = false

    init {
        on<ClientChatReceivedEvent>().filter {
            enabled && EssentialAPI.getMinecraftUtil().isHypixel() && type.toInt() != 2
        }.subscribe {
            val msg = message.unformattedText.stripControlCodes()
            if (triggers.any { it.matcher(msg).matches() }) {
                if (isAutoGGLoaded && waitForGG) {
                    shouldDetectGG = true
                } else sendCommand()
            } else if (shouldDetectGG) {
                if (ggPattern.matcher(msg).find()) {
                    sentGG = true
                    shouldDetectGG = false
                }
            } else if (sentGG) {
                if (karmaPattern.matcher(msg).matches()) {
                    sendCommand()
                }
            }
        }

        on<HypixelServerChangeEvent>().subscribe {
            shouldDetectGG = false
            sentGG = false
        }
    }

    private fun sendCommand() {
        Multithreading.schedule({ mc.thePlayer.sendChatMessage(command) }, delay.toLong(), TimeUnit.SECONDS)
    }
}