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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.hypixel.Hypixel
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler

object HypixelCommand : Command("currentdata") {
    @DefaultHandler
    fun handle() {
        sendPrefixMessage("Current Hypixel Game: ${Hypixel.currentGame}\n")

        Hypixel.currentProperty.map { "Property key: ${it.key} Value: ${it.value}" }.forEach(::sendPrefixMessage)

        sendPrefixMessage("\n${Hypixel.locrawInfo}")
    }
}