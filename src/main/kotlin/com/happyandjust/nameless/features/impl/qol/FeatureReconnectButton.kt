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

import com.happyandjust.nameless.devqol.LOGGER
import com.happyandjust.nameless.devqol.formatDouble
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.transformToPrecision
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.ScreenActionPerformedListener
import com.happyandjust.nameless.features.listener.ScreenInitListener
import com.happyandjust.nameless.mixins.accessors.AccessorGuiDisconnected
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CInt
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiDisconnected
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.multiplayer.ServerData
import net.minecraftforge.client.event.GuiScreenEvent

object FeatureReconnectButton : SimpleFeature(
    Category.QOL,
    "reconnectbutton",
    "Add Reconnect Button",
    "Add reconnect button when you got disconnected",
    true
), ScreenInitListener, ScreenActionPerformedListener, ClientTickListener {

    private var lastServerData: ServerData? = null
    private val gap = 8
    private var currentDisconnectInfo: DisconnectInfo? = null

    init {
        parameters["auto"] = FeatureParameter(
            0,
            "autoreconnect",
            "enable",
            "Enable Auto Reconnect",
            "If this is enabled, mod will auto reconnect to server in seconds you set",
            true,
            CBoolean
        ).also {
            it.parameters["second"] = FeatureParameter(
                0,
                "autoreconnect",
                "second",
                "Auto Reconnect Second",
                "",
                5,
                CInt
            ).also { featureParameter ->
                featureParameter.minValue = 1.0
                featureParameter.maxValue = 60.0
            }
        }
    }

    override fun actionPerformedPre(e: GuiScreenEvent.ActionPerformedEvent.Pre) {

    }

    override fun actionPerformedPost(e: GuiScreenEvent.ActionPerformedEvent.Post) {
        val gui = e.gui
        if (e.button.id == 101 && gui is AccessorGuiDisconnected && enabled) {
            val guiConnecting = GuiConnecting(gui.parentScreen, mc, lastServerData!!)
            mc.displayGuiScreen(guiConnecting)
        }
    }

    override fun screenInitPre(e: GuiScreenEvent.InitGuiEvent.Pre) {
    }

    override fun screenInitPost(e: GuiScreenEvent.InitGuiEvent.Post) {
        when (val gui = e.gui) {
            is GuiDisconnected -> {
                if (enabled) {
                    if (lastServerData != null) { // This should not be happened

                        for (button in e.buttonList) {
                            if (button.id == 0) {

                                val sec = getParameter<Boolean>("auto").getParameterValue<Int>("second")
                                val auto = getParameterValue<Boolean>("auto")
                                val text = if (auto) "Reconnect in: ${getSecondText(sec.toDouble())}" else "Reconnect"
                                e.buttonList.add(
                                    GuiButton(
                                        101,
                                        button.xPosition,
                                        button.yPosition,
                                        (button.width / 2) - (gap - 2),
                                        button.height,
                                        text
                                    ).also {
                                        button.xPosition = button.xPosition + button.width - it.width

                                        button.width = it.width

                                        if (auto) {
                                            currentDisconnectInfo =
                                                DisconnectInfo(
                                                    System.currentTimeMillis() + (sec * 1000),
                                                    it
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

                                break
                            }
                        }

                    } else {
                        LOGGER.error("ERROR: ServerData is null, how tf you got disconnected from server")
                    }
                }
            }
            is GuiConnecting -> lastServerData = mc.currentServerData
        }

    }

    override fun tick() {

    }

    override fun tickWorldNull() {
        getDisconnectInfo()?.let {
            val remainTime = it.shouldReconnectTime - System.currentTimeMillis()

            if (remainTime <= 0) {
                currentDisconnectInfo = null
                mc.displayGuiScreen(it.guiConnecting())
                return
            }

            val sec = remainTime / 1000.0

            it.reconnectButton.displayString = "Reconnect in: ${getSecondText(sec)}"
        }
    }

    /**
     * assume max: 10 seconds
     * 0 ~ 3: red
     * 4 ~ 10: green
     */
    private fun getSecondText(currentSecond: Double): String {
        val max = getParameter<Boolean>("auto").getParameterValue<Int>("second")

        val percent = max * 0.3

        val colorCode = if (currentSecond <= percent) "§4" else "§a"

        return "$colorCode${currentSecond.transformToPrecision(1).formatDouble()}s"
    }

    private fun getDisconnectInfo() = currentDisconnectInfo?.takeIf {
        enabled && getParameterValue("auto") && mc.currentScreen is GuiDisconnected
    }

    data class DisconnectInfo(
        val shouldReconnectTime: Long,
        val reconnectButton: GuiButton,
        val guiConnecting: () -> GuiConnecting
    )
}
