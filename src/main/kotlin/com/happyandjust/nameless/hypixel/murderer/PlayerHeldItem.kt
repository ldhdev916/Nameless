package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.dsl.mc
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S09PacketHeldItemChange

object PlayerHeldItemFactory {
    fun createImpl(packet: Any) = when (packet) {
        is S04PacketEntityEquipment -> {
            mc.theWorld.getEntityByID(packet.entityID) as? EntityPlayer to packet.itemStack
        }
        is S09PacketHeldItemChange -> {
            mc.thePlayer to mc.thePlayer.inventory.getStackInSlot(packet.heldItemHotbarIndex)
        }
        else -> null to null
    }
}