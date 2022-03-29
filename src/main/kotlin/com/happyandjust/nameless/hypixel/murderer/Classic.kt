package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.sendDebugMessage
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.base.ParameterHierarchy
import com.happyandjust.nameless.features.impl.qol.MurdererFinder
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S09PacketHeldItemChange
import java.awt.Color

class Classic : MurdererMode {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = MurdererFinder.classic

    private val murderers = object : HashSet<String>() {
        override fun add(element: String): Boolean {
            val newlyAdded = super.add(element)
            if (newlyAdded) {
                if (notifyMurderer) {
                    val message = notifyMessage.replace("&", "§").replace("{name}", element)
                    sendPrefixMessage(message)
                }
                sendDebugMessage("Classic", "Added murderer: $element")
            }

            return newlyAdded
        }
    }

    override fun registerEventListeners() {
        on<PacketEvent.Received>().addSubscribe {
            val (entityPlayer, heldItem) = when (val msg = packet) {
                is S04PacketEntityEquipment -> {
                    mc.theWorld.getEntityByID(msg.entityID) as? EntityPlayer to msg.itemStack
                }
                is S09PacketHeldItemChange -> {
                    val player = mc.thePlayer
                    player to player?.inventory?.getStackInSlot(msg.heldItemHotbarIndex)
                }
                else -> return@addSubscribe
            }
            entityPlayer ?: return@addSubscribe
            heldItem ?: return@addSubscribe

            if (heldItem.item in MurdererFinder.sword_list) {
                murderers.add(entityPlayer.name)
            }
        }

        on<OutlineRenderEvent>().filter { entity is EntityPlayer && entity.name in murderers }.addSubscribe {
            colorInfo = ColorInfo(murdererColor.rgb, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : MurdererModeCreator {
        override fun createImpl() = Classic()

        override val modes = setOf("MURDER_CLASSIC", "MURDER_DOUBLE_UP")

        private var murdererColor by parameter(Color.blue.toChromaColor()) {
            key = "murdererColor"
            title = "Murderer Color"
        }

        private var notifyMurderer by parameter(true) {
            key = "notifyMurderer"
            title = "Notify Murderer"
            desc = "Notify murderer's name in chat when new murderer is detected"
        }

        // TODO Create new class that automatically converts & into § and {some_name} into value
        private var notifyMessage by parameter("&cMurderer: {name}") {
            key = "notifyMessage"
            title = "Notify Message"
            desc = "{name}: Murderer's name"
        }

        override fun ParameterHierarchy.setupHierarchy() {
            +::murdererColor

            ::notifyMurderer {
                +::notifyMessage
            }
        }
    }
}