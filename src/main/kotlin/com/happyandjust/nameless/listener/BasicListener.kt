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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.devqol.LOGGER
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.CurrentPlayerJoinWorldEvent
import com.happyandjust.nameless.gui.EGui
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

class BasicListener {

    @SubscribeEvent
    fun onWorldJoin(e: EntityJoinWorldEvent) {
        if (e.entity == mc.thePlayer) {
            val event = CurrentPlayerJoinWorldEvent(e.world)
            MinecraftForge.EVENT_BUS.post(event)
        }
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        if (Nameless.INSTANCE.keyBindings[KeyBindingCategory.OPEN_GUI]!!.isKeyDown) {
            mc.displayGuiScreen(EGui())
        }
    }

    @SubscribeEvent
    fun onClientConnectedToServer(e: FMLNetworkEvent.ClientConnectedToServerEvent) {
        e.manager.channel().pipeline().addBefore("packet_handler", "namelees_packet_handler", PacketHandler())
        LOGGER.info("Added Packet Handler")
    }
}
