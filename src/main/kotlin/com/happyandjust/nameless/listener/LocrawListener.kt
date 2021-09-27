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
import com.happyandjust.nameless.devqol.inHypixel
import com.happyandjust.nameless.devqol.matchesMatcher
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.CurrentPlayerJoinWorldEvent
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.LocrawInfo
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Pattern
import kotlin.concurrent.timerTask

class LocrawListener {

    private var sentCommand = false
    private val JSON = Pattern.compile("\\{.+}")
    private val gson = Gson()
    private var lastJoined = -1L
    private var updateTick = 0
    private val threadPool = Executors.newFixedThreadPool(2)
    private val locrawTimer = Timer()

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        val entityPlayerSP = mc.thePlayer ?: return

        updateTick = (updateTick + 1) % 40

        if (updateTick == 0) {
            if (entityPlayerSP.inHypixel()) {
                threadPool.execute { Hypixel.updateGame() }
            } else {
                if (Hypixel.currentGame != null) Hypixel.updateGame()
            }
        }
    }


    @SubscribeEvent
    fun onWorldJoin(e: CurrentPlayerJoinWorldEvent) {
        if (mc.thePlayer.inHypixel()) {
            System.currentTimeMillis().also {
                if (it - lastJoined >= 1500) { // are you changing lobby every 1.5 seconds??
                    sentCommand = true
                    mc.thePlayer.sendChatMessage("/locraw")

                    locrawTimer.schedule(timerTask {  // in case somewhat error occurs
                        sentCommand = true
                        mc.thePlayer.sendChatMessage("/locraw")
                    }, 2000L)
                }

                lastJoined = it
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    fun onChatReceived(e: ClientChatReceivedEvent) {
        if (!sentCommand) return

        val msg = e.message.unformattedText
        JSON.matchesMatcher(msg) {
            Hypixel.apply {
                e.isCanceled = true
                locrawInfo = gson.fromJson(msg, LocrawInfo::class.java)

                updateGame()

                sentCommand = false
            }
        }
    }
}