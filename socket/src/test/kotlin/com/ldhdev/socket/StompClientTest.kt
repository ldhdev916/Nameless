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

import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.time.Duration.Companion.seconds

internal class StompClientTest {

    private fun getClient(playerUUID: String) =
        StompClient(URI("ws://localhost/nameless/stomp"), playerUUID, "1.0.5-Pre")

    private suspend inline fun StompClient.runCycle(action: StompClient.() -> Unit) {
        connectBlocking()

        delay(2.seconds)

        action()

        delay(2.seconds)

        disconnect()
    }

    @Test
    fun socketTest() {
    }
}