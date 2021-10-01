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
import com.happyandjust.nameless.devqol.inHypixel
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.events.CurrentPlayerJoinWorldEvent
import com.happyandjust.nameless.gui.EGui
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.mixinhooks.FontRendererHook
import com.happyandjust.nameless.utils.Utils
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent

class BasicListener {

    private var tick = 0

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
        if (Nameless.INSTANCE.keyBindings[KeyBindingCategory.AUTO_WDR]!!.isKeyDown && mc.thePlayer.inHypixel()) {

            val tab = Utils.getPlayersInTab()

            val players =
                mc.theWorld.playerEntities.filter { tab.contains(it) && it != mc.thePlayer }
                    .sortedBy { mc.thePlayer.getDistanceToEntity(it) }
            if (players.isEmpty()) return

            val player = players[0]

            sendClientMessage("§aSending WatchDog Report to ${player.name} with Type bhop,ka,reach")
            mc.thePlayer.sendChatMessage("/wdr ${player.name} bhop ka reach")
        }
    }

    @SubscribeEvent
    fun onClientConnectedToServer(e: FMLNetworkEvent.ClientConnectedToServerEvent) {
        e.manager.channel().pipeline().addBefore("packet_handler", "namelees_packet_handler", PacketHandler())
        LOGGER.info("Added Packet Handler")
    }

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) return

        tick = (tick + 1) % (20 * 60)

        if (tick == 0) {
            FontRendererHook.cache.clear()
        }
    }
}
