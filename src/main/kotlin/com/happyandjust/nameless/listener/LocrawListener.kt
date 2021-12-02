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

package com.happyandjust.nameless.listener

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.happyandjust.nameless.dsl.matchesMatcher
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.LocrawInfo
import gg.essential.api.EssentialAPI
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.regex.Pattern

object LocrawListener {

    private var sentCommand = false
    private val JSON = Pattern.compile("\\{.+}")
    private val gson = Gson()
    private var updateTick = 0
    private var locrawTick = 0

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) return
        val entityPlayerSP = mc.thePlayer ?: return

        locrawTick++
        if (locrawTick == 20 && EssentialAPI.getMinecraftUtil().isHypixel()) {
            sentCommand = true
            entityPlayerSP.sendChatMessage("/locraw")
        }

        updateTick = (updateTick + 1) % 40

        if (updateTick == 0) {
            Hypixel.updateGame()
        }
    }


    @SubscribeEvent
    fun onWorldLoad(e: WorldEvent.Load) {
        locrawTick = 0
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceived(e: ClientChatReceivedEvent) {

        val msg = e.message.unformattedText
        JSON.matchesMatcher(msg) {
            Hypixel.apply {
                if (sentCommand) {
                    e.isCanceled = true
                }
                try {
                    locrawInfo = gson.fromJson(msg, LocrawInfo::class.java)

                    updateGame()
                } catch (ignored: JsonSyntaxException) {

                }

                if (sentCommand) {
                    sentCommand = false
                }
            }
        }
    }
}