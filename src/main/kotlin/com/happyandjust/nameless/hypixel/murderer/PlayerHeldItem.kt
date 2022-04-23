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

package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.dsl.mc
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S09PacketHeldItemChange

object PlayerHeldItemFactory {
    fun createImpl(packet: Any) = when (packet) {
        is S04PacketEntityEquipment -> {
            mc.theWorld?.getEntityByID(packet.entityID) as? EntityPlayer to packet.itemStack
        }
        is S09PacketHeldItemChange -> {
            mc.thePlayer to mc.thePlayer?.inventory?.getStackInSlot(packet.heldItemHotbarIndex)
        }
        else -> null to null
    }
}