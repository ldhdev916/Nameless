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
import com.happyandjust.nameless.dsl.sendClientMessage
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.listener.WaypointListener
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object WaypointCommand : ClientCommandBase("waypoint") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {

        if (args.size == 1 && args[0] == "clear") {
            WaypointListener.currentWaypointInfo = null
            return
        }

        if (args.size != 4 && args.size != 3) {
            sendPrefixMessage("§cUsage: /waypoint [x] [y] [z] (canFly|true|false) or /waypoint clear")
            return
        }

        val canFly = if (args.size == 3) true else args[3].toBoolean()

        WaypointListener.currentWaypointInfo =
            WaypointListener.WaypointInfo(
                BlockPos(
                    args[0].toInt(),
                    args[1].toInt(),
                    args[2].toInt()
                ).also { sendClientMessage("§aNow targeting $it") },
                canFly
            )
    }
}