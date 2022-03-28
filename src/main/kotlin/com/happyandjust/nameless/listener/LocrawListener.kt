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

package com.happyandjust.nameless.listener

import com.happyandjust.nameless.dsl.cancel
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.pureText
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.hypixel.Hypixel
import gg.essential.api.EssentialAPI
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent

object LocrawListener {

    private var sentCommand = false
    private val JSON = "\\{\"server\".+}".toRegex()
    private var locrawTick = 0
    private val locrawJson = Json { ignoreUnknownKeys = true }

    init {
        on<SpecialTickEvent>().filter { EssentialAPI.getMinecraftUtil().isHypixel() && ++locrawTick == 20 }.subscribe {
            sentCommand = true
            mc.thePlayer.sendChatMessage("/locraw")
        }
        on<WorldEvent.Load>().subscribe {
            locrawTick = 0
        }

        on<ClientChatReceivedEvent>().subscribe {
            if (pureText.matches(JSON)) {
                with(Hypixel) {
                    if (sentCommand) {
                        cancel()
                    }
                    val prev = locrawInfo
                    runCatching {
                        locrawInfo = locrawJson.decodeFromString(pureText)

                        updateGame()
                    }.onFailure {
                        it.printStackTrace()
                        locrawInfo = prev
                    }

                    sentCommand = false
                }
            }
        }
    }
}