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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.devqol.toBlockPos
import com.happyandjust.nameless.listener.WaypointListener
import net.minecraft.command.ICommandSender

object WaypointCommand : ClientCommandBase("waypoint") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {

        if (args.size == 1 && args[0] == "clear") {
            WaypointListener.currentWaypointInfo = null
            return
        }

        if (args.size != 4 && args.size != 3) {
            sendClientMessage("§cUsage: /waypoint [x] [y] [z] (canFly|true|false) or /waypoint clear")
            return
        }

        val canFly = if (args.size == 3) true else args[3].toBoolean()

        ///waypoint -6 108 -33 false

        WaypointListener.currentWaypointInfo = WaypointListener.WaypointInfo(args.toBlockPos(0..2).also {
            sendClientMessage("§aNow targeting $it")
        }, canFly)
    }
}