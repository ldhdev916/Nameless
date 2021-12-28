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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.events.KeyPressEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.COverlay
import com.happyandjust.nameless.serialization.converters.CString
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.util.Vec3

object FeatureBlockMovementPacket : BlatantFeature(
    Category.GENERAL,
    "blockmovementpacket",
    "Block Movement Packet",
    "Blocks movement packet from sending to server"
) {
    override var useAccepted by ConfigValue("blockmovementpacket", "accept", false, CBoolean)
    override val reasonForBlatant = "Modifying Packet"
    private var prevOnGround = false
    private var lastPositition: Vec3? = null

    @SubParameterOf("notifyBlocking")
    private var notifyText: String by FeatureParameter(
        0,
        "blockmovementpacket",
        "text",
        "Notify Text",
        "Text that will render on your screen",
        "&cBlocking Packet!",
        CString
    ).apply {
        onValueChange = { textState.set(it.replace("&", "§")) }
    }
    private val textState = BasicState(notifyText.replace("&", "§"))

    private var notifyBlocking by object : OverlayParameter<Boolean>(
        0,
        "blockmovementpacket",
        "notify",
        "Notify When Enabled",
        "Render some text on your screen when this feature is enabled",
        true,
        CBoolean
    ) {
        override var overlayPoint by ConfigValue("blockmovementpacket", "overlay", Overlay.DEFAULT, COverlay)
        private val window = Window().apply {
            UIText().bindText(textState).constrain {
                x = basicXConstraint { overlayPoint.point.x.toFloat() }.fixed()
                y = basicYConstraint { overlayPoint.point.y.toFloat() }.fixed()

                textScale = basicTextScaleConstraint { overlayPoint.scale.toFloat() }.fixed()
            } childOf this
        }

        override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
            return UIText(notifyText.replace("&", "§")).constrain {
                textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()
            }
        }

        override fun shouldDisplayInRelocateGui() = value

        override fun renderOverlay0(partialTicks: Float) {
            if (enabled && value) window.draw()
        }

        override fun getDisplayName() = this@FeatureBlockMovementPacket.title
    }

    init {
        on<PacketEvent.Sending>().filter { enabled && mc.renderViewEntity == mc.thePlayer }.subscribe {
            packet.withInstance<C03PacketPlayer> {
                packet = C03PacketPlayer(prevOnGround)
            }
        }

        on<FeatureStateChangeEvent.Pre>().filter { feature == this@FeatureBlockMovementPacket && enabledAfter }
            .subscribe {
                if (mc.renderViewEntity != mc.thePlayer) {
                    sendPrefixMessage("§cYou're spectating something!")
                    cancel()
                } else {
                    lastPositition = mc.thePlayer.toVec3()
                }
            }

        on<FeatureStateChangeEvent.Post>().filter { feature == this@FeatureBlockMovementPacket }
            .subscribe {
                if (enabledAfter) {
                    prevOnGround = mc.thePlayer.onGround
                } else {
                    lastPositition?.let {
                        mc.thePlayer.setPositionAndUpdate(it.xCoord, it.yCoord, it.zCoord)
                    }
                }
            }

        on<KeyPressEvent>().filter { keyBindingCategory == KeyBindingCategory.BLOCK_MOVEMENT_PACKET && !inGui && isNew }
            .subscribe {
                invertEnableState()
            }
    }
}