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
import com.happyandjust.nameless.features.impl.settings.FeatureReparty
import net.minecraft.command.ICommandSender

object RepartyCommand : ClientCommandBase("reparty") {

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        with(FeatureReparty) {
            doingReparty = true
            currentPartyPhase = FeatureReparty.Phase.CHECKING_MEMBERS

            commandScheduler.add("/pl")
        }
    }

    override fun getCommandAliases() = mutableListOf("rp", "rparty")
}