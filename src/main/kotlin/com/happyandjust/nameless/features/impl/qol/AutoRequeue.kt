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

import com.happyandjust.nameless.dsl.fetch
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.stripControlCodes
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.Multithreading
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.Loader
import java.util.concurrent.TimeUnit

object AutoRequeue : SimpleFeature(
    "autoRequeue",
    "Auto Requeue",
    "Auto send /play command you set after game end\n§cMake sure to set hypixel language as English"
) {

    init {
        hierarchy {
            +::command

            +::delay

            if (isAutoGGLoaded) {
                +::waitForGG
            }
        }
    }

    private var command by parameter("/play ranked_normal") {
        key = "command"
        title = "Play Command"
        desc = "The play command mod will send after game end"
    }

    private var delay by parameter(0) {
        key = "delay"
        title = "Delay"
        desc = "Send delay in seconds"

        settings {
            maxValueInt = 5
        }
    }

    private var waitForGG by parameter(true) {
        key = "waitForGG"
        title = "Wait GG"
        desc = "Wait until AutoGG send gg message then send command"
    }
    private val isAutoGGLoaded by lazy { Loader.isModLoaded("autogg") }

    private val triggers = run {
        val json = Json { ignoreUnknownKeys = true }
        val response = "https://static.sk1er.club/autogg/regex_triggers_3.json".fetch()
        val triggerResponse = json.decodeFromString<TriggerResponse>(response)

        triggerResponse.servers
            .single { it.name == "Hypixel Server" }
            .triggers
            .filter { it.type == 0 }
            .map { it.pattern.toRegex() }
    }

    private val ggRegex = "\\w: gg".toRegex()
    private val karmaRegex = "\\+\\d+ Karma!".toRegex()

    private var shouldDetectGG = false
    private var sentGG = false

    init {
        on<ClientChatReceivedEvent>().filter {
            enabled && EssentialAPI.getMinecraftUtil().isHypixel() && type.toInt() != 2
        }.subscribe {
            val msg = message.unformattedText.stripControlCodes()
            when {
                triggers.any { it.matches(msg) } -> {
                    if (isAutoGGLoaded && waitForGG) {
                        shouldDetectGG = true
                    } else {
                        sendCommand()
                    }
                }
                shouldDetectGG && ggRegex.matches(msg) -> {
                    sentGG = true
                    shouldDetectGG = false
                }
                sentGG && karmaRegex.matches(msg) -> sendCommand()
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

    @kotlinx.serialization.Serializable
    private data class TriggerResponse(val servers: List<TriggerServer>)

    @kotlinx.serialization.Serializable
    private data class TriggerServer(val name: String, val triggers: List<TriggerData>)

    @kotlinx.serialization.Serializable
    private data class TriggerData(val type: Int, val pattern: String)
}