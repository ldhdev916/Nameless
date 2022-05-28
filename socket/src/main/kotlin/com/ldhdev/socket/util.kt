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

package com.ldhdev.socket

import com.ldhdev.namelessstd.Route
import com.ldhdev.namelessstd.Variable
import com.ldhdev.namelessstd.withVariables
import com.ldhdev.socket.data.StompSend
import com.ldhdev.socket.subscription.StompSubscription
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun StompClient.requestOnlinePlayers(action: (List<String>) -> Unit) {
    var subscription: StompSubscription? = null
    subscription = StompSubscription(Route.Client.SendOnlines.withVariables(Variable.Id to uuidIdentifier)) {
        val players = Json.decodeFromString<List<String>>(it.payload!!)
        action(players)
        unsubscribe(subscription!!)
    }
    subscribe(subscription)
    send(StompSend(Route.Server.ViewOnline))
}