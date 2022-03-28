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

package com.happyandjust.nameless.dsl

import com.happyandjust.nameless.features.impl.settings.Debug
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge

fun sendPrefixMessage(o: Any?) {
    sendPrefixMessage(ChatComponentText(o.toString()))
}

fun sendPrefixMessage(chatComponent: IChatComponent?) {
    sendClientMessage(
        ChatComponentText("§6[§3Nameless§6]§r ").appendSibling(
            chatComponent ?: ChatComponentText("null")
        )
    )
}

fun sendClientMessage(o: Any?) {
    sendClientMessage(ChatComponentText(o.toString()))
}

fun sendClientMessage(chatComponent: IChatComponent?) {
    val sendingChatComponent = chatComponent ?: ChatComponentText("null")

    val player = mc.thePlayer
    if (player == null) {
        LOGGER.info("[CHAT] ${sendingChatComponent.formattedText}")
        return
    }
    if (!MinecraftForge.EVENT_BUS.post(ClientChatReceivedEvent(1, sendingChatComponent))) {
        player.addChatMessage(sendingChatComponent)
    }
}

fun sendDebugMessage(tag: String, o: Any?) {
    if (!Debug.enabled) return
    sendDebugMessage("§6[§3$tag§6]§r $o")
}

fun sendDebugMessage(o: Any?) {
    if (!Debug.enabled) return
    sendDebugMessage(ChatComponentText(o.toString()))
}

fun sendDebugMessage(chatComponent: IChatComponent?) {
    if (!Debug.enabled) return
    val sendingChatComponent = chatComponent ?: ChatComponentText("null")

    val player = mc.thePlayer
    if (player == null) {
        LOGGER.info("[DEBUG] ${sendingChatComponent.formattedText}")
        return
    }
    val debugChat = ChatComponentText("§6[§3Debug§6]§r ").appendSibling(chatComponent)
    player.addChatMessage(debugChat)
}