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

import com.happyandjust.nameless.dsl.cancel
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.base.SimpleFeature
import gg.essential.api.EssentialAPI
import net.minecraftforge.client.event.ClientChatReceivedEvent

object HideTipMessage : SimpleFeature(
    Category.QOL,
    "hidetipmessage",
    "Hide Tip Message",
    "Hide you tipped SomePlayer in SomeGame! Message in Hypixel"
) {

    private val TIP_MESSAGE = "§aYou tipped \\w+ in .+!".toRegex()
    private val TIP_ALL = "§aYou tipped \\d+ players in \\d+ different games!".toRegex()

    init {
        on<ClientChatReceivedEvent>().filter {
            type.toInt() != 2 && enabled && EssentialAPI.getMinecraftUtil().isHypixel() && arrayOf(
                TIP_ALL,
                TIP_MESSAGE
            ).any(message.unformattedText::matches)
        }.subscribe {
            cancel()
        }
    }
}