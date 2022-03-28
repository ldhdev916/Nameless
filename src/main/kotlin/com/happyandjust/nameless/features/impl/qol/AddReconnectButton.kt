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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.dsl.withPrecisionText
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.mixins.accessors.AccessorGuiDisconnected
import gg.essential.api.utils.GuiUtil
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AddReconnectButton : SimpleFeature(
    "reconnectButton",
    "Add Reconnect Button",
    "Add reconnect button when you got disconnected",
    true
) {

    init {
        hierarchy {
            ::auto {
                +::autoSecond
            }
        }
    }

    private var lastServerData: ServerData? = null
    private const val gap = 8
    private var currentDisconnectInfo: DisconnectInfo? = null

    private var auto by parameter(true) {
        matchKeyCategory()
        key = "auto"
        title = "Enable Auto Reconnect"
        desc = "If this is enabled, mod will auto reconnect to server in seconds you set"
    }

    private var autoSecond by parameter(5) {
        matchKeyCategory()
        key = "second"
        title = "Auto Reconnect Second"

        settings {
            minValueInt = 1
            maxValueInt = 60
        }
    }

    init {
        on<GuiScreenEvent.ActionPerformedEvent.Post>().filter { button.id == 101 && enabled }.subscribe {
            withInstance<AccessorGuiDisconnected>(gui) {
                GuiUtil.open(GuiConnecting(parentScreen, mc, lastServerData!!))
            }
        }

        on<GuiScreenEvent.InitGuiEvent.Post>().subscribe {
            when (val gui = gui) {
                is GuiDisconnected -> {
                    if (enabled && lastServerData != null) {
                        buttonList.find { it.id == 0 }?.let {
                            val text = if (auto) "Reconnect in: ${getSecondText(autoSecond)}" else "Reconnect"
                            val reconnectButton = getReconnectButton(it, text)

                            it.width = reconnectButton.width
                            if (auto) {
                                val reconnectTime = System.currentTimeMillis() + (autoSecond * 1000)
                                val guiCallback = {
                                    val parentScreen = (gui as AccessorGuiDisconnected).parentScreen
                                    GuiConnecting(parentScreen, mc, lastServerData!!)
                                }

                                currentDisconnectInfo = DisconnectInfo(reconnectTime, reconnectButton, guiCallback)
                            }

                            buttonList.add(reconnectButton)
                        }
                    }
                }
                is GuiConnecting -> lastServerData = mc.currentServerData
            }
        }

        on<TickEvent.ClientTickEvent>().filter { phase == TickEvent.Phase.END }.subscribe {
            getDisconnectInfo()?.let {
                val remainTime = it.reconnectTime - System.currentTimeMillis()

                if (remainTime <= 0) {
                    currentDisconnectInfo = null
                    mc.displayGuiScreen(it.guiConnecting())
                    return@subscribe
                }

                val sec = remainTime / 1000.0

                it.reconnectButton.displayString = "Reconnect in: ${getSecondText(sec)}"
            }
        }
    }

    /**
     * assume max: 10 seconds
     * 0 ~ 3: red
     * 4 ~ 10: green
     */
    private fun getSecondText(currentSecond: Number): String {
        val percent = autoSecond * 0.3

        val colorCode = if (currentSecond.toDouble() <= percent) "§4" else "§a"

        return "$colorCode${currentSecond.toDouble().withPrecisionText(1)}s"
    }

    private fun getDisconnectInfo() = currentDisconnectInfo?.takeIf {
        enabled && auto && mc.currentScreen is GuiDisconnected
    }

    data class DisconnectInfo(
        val reconnectTime: Long,
        val reconnectButton: GuiButton,
        val guiConnecting: () -> GuiConnecting
    )

    private fun getReconnectButton(base: GuiButton, text: String): GuiButton {
        val width = base.width / 2 - gap / 2
        val x = base.xPosition + gap + width

        return GuiButton(101, x, base.yPosition, width, base.height, text)
    }
}