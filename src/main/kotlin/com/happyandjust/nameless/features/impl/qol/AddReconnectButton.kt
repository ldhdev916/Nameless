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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.transformToPrecisionString
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.mixins.accessors.AccessorGuiDisconnected
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CInt
import gg.essential.api.utils.GuiUtil
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AddReconnectButton : SimpleFeature(
    Category.QOL,
    "reconnectbutton",
    "Add Reconnect Button",
    "Add reconnect button when you got disconnected",
    true
) {

    private var lastServerData: ServerData? = null
    private const val gap = 8
    private var currentDisconnectInfo: DisconnectInfo? = null

    private var auto by FeatureParameter(
        0,
        "autoreconnect",
        "enable",
        "Enable Auto Reconnect",
        "If this is enabled, mod will auto reconnect to server in seconds you set",
        true,
        CBoolean
    )

    @SubParameterOf("auto")
    private var autoSecond by FeatureParameter(
        0,
        "autoreconnect",
        "second",
        "Auto Reconnect Second",
        "",
        5,
        CInt
    ).apply {
        minValue = 1.0
        maxValue = 60.0
    }

    init {
        on<GuiScreenEvent.ActionPerformedEvent.Post>().filter { button.id == 101 && enabled }.subscribe {
            gui.withInstance<AccessorGuiDisconnected> {
                GuiUtil.open(GuiConnecting(parentScreen, mc, lastServerData!!))
            }
        }

        on<GuiScreenEvent.InitGuiEvent.Post>().subscribe {
            when (val gui = gui) {
                is GuiDisconnected -> {
                    if (enabled && lastServerData != null) {
                        buttonList.find { it.id == 0 }?.let {
                            val text =
                                if (auto) "Reconnect in: ${getSecondText(autoSecond.toDouble())}" else "Reconnect"
                            buttonList.add(
                                GuiButton(
                                    101,
                                    it.xPosition,
                                    it.yPosition,
                                    (it.width / 2) - (gap - 2),
                                    it.height,
                                    text
                                ).apply {
                                    it.xPosition = it.xPosition + it.width - width

                                    it.width = width

                                    if (auto) {
                                        currentDisconnectInfo =
                                            DisconnectInfo(
                                                System.currentTimeMillis() + (autoSecond * 1000),
                                                this
                                            ) {
                                                GuiConnecting(
                                                    (gui as AccessorGuiDisconnected).parentScreen,
                                                    mc,
                                                    lastServerData!!
                                                )
                                            }
                                    }
                                }
                            )
                        }
                    }
                }
                is GuiConnecting -> lastServerData = mc.currentServerData
            }
        }

        on<TickEvent.ClientTickEvent>().filter { phase == TickEvent.Phase.END }.subscribe {
            getDisconnectInfo()?.let {
                val remainTime = it.shouldReconnectTime - System.currentTimeMillis()

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
    private fun getSecondText(currentSecond: Double): String {
        val percent = autoSecond * 0.3

        val colorCode = if (currentSecond <= percent) "§4" else "§a"

        return "$colorCode${currentSecond.transformToPrecisionString(1)}s"
    }

    private fun getDisconnectInfo() = currentDisconnectInfo?.takeIf {
        enabled && auto && mc.currentScreen is GuiDisconnected
    }

    data class DisconnectInfo(
        val shouldReconnectTime: Long,
        val reconnectButton: GuiButton,
        val guiConnecting: () -> GuiConnecting
    )
}