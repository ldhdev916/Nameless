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

package com.happyandjust.nameless.core

import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

abstract class ClientCommandBase(private val commandName: String) : CommandBase() {

    final override fun getCommandName() = commandName

    final override fun getCommandUsage(sender: ICommandSender?) = null

    abstract override fun processCommand(sender: ICommandSender, args: Array<out String>)

    final override fun canCommandSenderUseCommand(sender: ICommandSender?) = true
}
