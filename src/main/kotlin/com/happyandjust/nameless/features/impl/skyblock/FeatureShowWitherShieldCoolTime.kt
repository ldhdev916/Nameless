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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.PacketListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CInt
import com.happyandjust.nameless.serialization.converters.COverlay
import com.happyandjust.nameless.serialization.converters.CString
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

object FeatureShowWitherShieldCoolTime :
    OverlayFeature(Category.SKYBLOCK, "showwithershieldcooltime", "Show Wither Shield CoolTime", "", false),
    PacketListener, ClientTickListener {

    init {
        parameters["held"] = FeatureParameter(
            0,
            "withershield",
            "onlyheld",
            "Show only when you are holding Hyperion, Scylla, Valkyrie, Astraea",
            "",
            false,
            CBoolean
        )
        parameters["onlycd"] = FeatureParameter(
            0,
            "withershield",
            "onlycd",
            "Show only when wither shield is on cooltime",
            "",
            false,
            CBoolean
        )
        parameters["precision"] = FeatureParameter(
            1,
            "withershield",
            "precision",
            "Decimal Point Precision",
            "Decimal point precision of cooltime",
            3,
            CInt
        ).also {
            it.minValue = 0.0
            it.maxValue = 3.0
        }
        parameters["availtext"] = FeatureParameter(
            2,
            "withershield",
            "availtext",
            "Overlay Available Text",
            "Text when wither shield is ready",
            "&aShield: Ready",
            CString
        )
        parameters["text"] = FeatureParameter(
            3,
            "withershield",
            "text",
            "Overlay Text",
            "Text when wither shield is on cooltime",
            "&6Shield: {value}s",
            CString
        )
    }

    override val overlayPoint = ConfigValue("withershield", "overlay", Overlay.DEFAULT, COverlay)
    private var lastWitherShieldUse: Long? = null
        get() = field?.takeIf { it + 5000 > System.currentTimeMillis() }
    private val swords = hashSetOf("HYPERION", "VALKYRIE", "SCYLLA", "ASTRAEA")
    private var holdingSword = false
    private var scanTick = 0

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        return UIText(getParameterValue<String>("availtext").replace("&", "§")).constrain {
            textScale = relocateComponent.currentScale.pixels()

            relocateComponent.onScaleChange {
                textScale = it.pixels()
            }
        }
    }

    override fun shouldDisplayInRelocateGui(): Boolean {
        return enabled && Hypixel.currentGame == GameType.SKYBLOCK
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (enabled && Hypixel.currentGame == GameType.SKYBLOCK) {
            getText()?.let {
                matrix {
                    setup(overlayPoint.value)
                    mc.fontRendererObj.drawStringWithShadow(it.replace("&", "§"), 0f, 0f, 0xFFFFFFFF.toInt())
                }
            }
        }
    }

    private fun getText(): String? {
        if (getParameterValue("held") && !holdingSword) {
            return null
        }

        return lastWitherShieldUse?.let {
            val timeLeft = 5 - (System.currentTimeMillis() - it) / 1000.0
            val precision = getParameterValue<Int>("precision")

            getParameterValue<String>("text").replace(
                "{value}",
                timeLeft.transformToPrecisionString(precision)
            )
        } ?: if (getParameterValue("onlycd")) null else getParameterValue<String>("availtext")
    }

    override fun onSendingPacket(e: PacketEvent.Sending) {
        if (lastWitherShieldUse != null) return
        val msg = e.packet

        if (msg is C08PacketPlayerBlockPlacement && Hypixel.currentGame == GameType.SKYBLOCK) {
            if (swords.contains(msg.stack.getSkyBlockID())) {
                lastWitherShieldUse = System.currentTimeMillis()
            }
        }
    }

    override fun onReceivedPacket(e: PacketEvent.Received) {

    }

    override fun tick() {
        scanTick = (scanTick + 1) % 8
        if (scanTick == 0) {
            holdingSword = if (Hypixel.currentGame == GameType.SKYBLOCK) {
                mc.thePlayer.heldItem.getSkyBlockID() in swords
            } else false
        }

    }
}