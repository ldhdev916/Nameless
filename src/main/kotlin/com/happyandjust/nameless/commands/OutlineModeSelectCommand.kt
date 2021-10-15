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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.core.OutlineMode
import com.happyandjust.nameless.devqol.convertToStringList
import com.happyandjust.nameless.devqol.sendClientMessage
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object OutlineModeSelectCommand : ClientCommandBase("selectoutline") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.size == 1) {
            val outline = try {
                OutlineMode.valueOf(args[0].uppercase())
            } catch (e: Exception) {
                sendClientMessage("§cNo Such OutlineMode: ${args[0]}")
                return
            }

            sendClientMessage("§aSuccessfully Changed OutlineMode to $outline")
            Nameless.INSTANCE.selectedOutlineMode = outline
        } else {
            sendClientMessage(
                "§cUsage: /selectoutline ${OutlineMode.values().joinToString("|", "[", "]") { it.name }}"
            )
        }
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos
    ): MutableList<String> {

        if (args.size == 1) {
            return OutlineMode.values().filter { it.name.startsWith(args[0], true) }.toList()
                .convertToStringList { it.name }.toMutableList()
        }

        return super.addTabCompletionOptions(sender, args, pos)
    }
}