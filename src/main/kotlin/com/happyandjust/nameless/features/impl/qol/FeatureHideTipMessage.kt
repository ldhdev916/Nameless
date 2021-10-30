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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.devqol.inHypixel
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ChatListener
import net.minecraftforge.client.event.ClientChatReceivedEvent

object FeatureHideTipMessage : SimpleFeature(
    Category.QOL,
    "hidetipmessage",
    "Hide Tip Message",
    "Hide you tipped SomePlayer in SomeGame! Message in Hypixel"
), ChatListener {

    private val TIP_MESSAGE = "§aYou tipped \\w+ in .+!".toRegex()
    private val TIP_ALL = "§aYou tipped \\d+ players in \\d+ different games!".toRegex()

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (enabled && mc.thePlayer.inHypixel()) {
            val msg = e.message.unformattedText

            if (msg.matches(TIP_MESSAGE) || msg.matches(TIP_ALL)) {
                e.isCanceled = true
            }
        }
    }
}