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
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger
import java.io.PrintStream

private const val TESTING = false
private val debugSender
    get() = when {
        TESTING -> PrintStreamChatSender(System.out)
        mc.thePlayer != null -> PlayerDebugSender(mc.thePlayer)
        else -> LoggerDebugSender(LOGGER)
    }


private val chatSender
    get() = when {
        TESTING -> PrintStreamChatSender(System.out)
        mc.thePlayer != null -> PlayerChatSender(mc.thePlayer)
        else -> LoggerChatSender(LOGGER)
    }

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
    chatSender.send(chatComponent ?: ChatComponentText("null"))
}

fun sendDebugMessage(tag: String, o: Any?) {
    if (!Debug.enabled && !TESTING) return
    sendDebugMessage("§6[§3$tag§6]§r $o")
}

fun sendDebugMessage(o: Any?) {
    if (!Debug.enabled && !TESTING) return
    sendDebugMessage(ChatComponentText(o.toString()))
}

fun sendDebugMessage(chatComponent: IChatComponent?) {
    if (!Debug.enabled && !TESTING) return
    debugSender.send(chatComponent ?: ChatComponentText("null"))
}

private fun interface ChatSender {
    fun send(message: IChatComponent)
}

private class LoggerChatSender(private val logger: Logger) : ChatSender {
    override fun send(message: IChatComponent) {
        logger.info("[CHAT] ${message.formattedText}")
    }
}


private class LoggerDebugSender(private val logger: Logger) : ChatSender {
    override fun send(message: IChatComponent) {
        logger.info("[DEBUG] ${message.formattedText}")
    }
}

private class PlayerChatSender(private val player: EntityPlayerSP) : ChatSender {
    override fun send(message: IChatComponent) {
        if (MinecraftForge.EVENT_BUS.post(ClientChatReceivedEvent(1, message))) return
        player.addChatComponentMessage(message)
    }
}

private class PlayerDebugSender(private val player: EntityPlayerSP) : ChatSender {
    override fun send(message: IChatComponent) {
        player.addChatComponentMessage(ChatComponentText("§6[§3Debug§6]§r ").appendSibling(message))
    }
}

private class PrintStreamChatSender(private val printStream: PrintStream) : ChatSender {
    override fun send(message: IChatComponent) {
        printStream.println(message.unformattedText)
    }
}