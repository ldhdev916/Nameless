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

import com.happyandjust.nameless.events.PacketEvent
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import net.minecraftforge.common.MinecraftForge

class PacketHandler : ChannelDuplexHandler() {

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {

        var msg = msg ?: return

        val event = PacketEvent.Received(msg)

        if (MinecraftForge.EVENT_BUS.post(event)) return

        msg = event.packet

        super.channelRead(ctx, msg)
    }

    override fun write(ctx: ChannelHandlerContext?, msg: Any?, promise: ChannelPromise?) {

        var msg = msg ?: return

        val event = PacketEvent.Sending(msg)

        if (MinecraftForge.EVENT_BUS.post(event)) return

        msg = event.packet

        super.write(ctx, msg, promise)
    }
}