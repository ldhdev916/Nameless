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

import com.google.gson.JsonObject
import com.happyandjust.nameless.dsl.handler
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.stripControlCodes
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.serialization.converters.CString
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

object FeatureAutoRequeue : SimpleFeature(
    Category.QOL,
    "autorequeue",
    "Auto Requeue",
    "Auto send /play command you set after game end\n§cMake sure to set hypixel language as English"
) {
    private var playCommand by FeatureParameter(
        0,
        "autorq",
        "playcmd",
        "Play Command",
        "The play command mod will send after game end",
        "/play ranked_normal",
        CString
    )

    private var delay by FeatureParameter(1, "autorq", "delay", "Delay", "Send delay in seconds", 0, CInt).apply {
        minValue = 0.0
        maxValue = 5.0
    }

    private var waitForGG by FeatureParameter(
        2,
        "autorq",
        "waitgg",
        "Wait GG",
        "Wait until AutoGG send gg message then send command",
        true,
        CBoolean
    )
    var isAutoGGLoaded by Delegates.notNull<Boolean>()
    private val triggers by lazy {
        val json = "https://static.sk1er.club/autogg/regex_triggers_3.json".handler().read(JsonObject())
        json["servers"].asJsonArray[0].asJsonObject["triggers"].asJsonArray.mapNotNull {
            if (it.asJsonObject["type"].asInt == 0) it.asJsonObject["pattern"].asString.toPattern() else null
        }
    }

    private val ggPattern = "\\w: gg".toPattern()
    private val karmaPattern = "\\+\\d+ Karma!".toPattern()

    fun fetchGameEndData() = triggers

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
        Multithreading.schedule({ mc.thePlayer.sendChatMessage(playCommand) }, delay.toLong(), TimeUnit.SECONDS)
    }
}