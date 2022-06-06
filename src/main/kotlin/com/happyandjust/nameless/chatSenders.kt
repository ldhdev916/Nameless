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

package com.happyandjust.nameless

import com.happyandjust.nameless.dsl.LOGGER
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.impl.settings.Debug
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger
import java.io.PrintStream

private const val TESTING = false

interface ChatSender {
    fun send(message: IChatComponent)

    companion object {
        operator fun invoke(): ChatSender = when {
            TESTING -> PrintStreamChatSender(System.out)
            mc.thePlayer != null -> PlayerChatSender(mc.thePlayer)
            else -> LoggerChatSender(LOGGER)
        }
    }
}

class PrintStreamChatSender(private val printStream: PrintStream) : ChatSender {
    override fun send(message: IChatComponent) {
        printStream.println(message.unformattedText)
    }
}

class LoggerChatSender(private val logger: Logger) : ChatSender {
    override fun send(message: IChatComponent) {
        logger.info("[INFO] ${message.formattedText}")
    }
}

class PlayerChatSender(private val player: EntityPlayerSP) : ChatSender {
    override fun send(message: IChatComponent) {
        if (MinecraftForge.EVENT_BUS.post(ClientChatReceivedEvent(1, message))) return
        player.addChatComponentMessage(message)
    }
}

// ------------------------------

interface DebugChatSender : ChatSender {

    fun shouldFilter(): Boolean = true

    override fun send(message: IChatComponent) {
        if (shouldFilter() && !Debug.enabled) return
        sendDebug(message)
    }

    fun sendDebug(message: IChatComponent)

    companion object {
        operator fun invoke(): DebugChatSender = when {
            TESTING -> PrintStreamDebugChatSender(System.out)
            mc.thePlayer != null -> PlayerDebugChatSender(mc.thePlayer)
            else -> LoggerDebugChatSender(LOGGER)
        }
    }
}

class PrintStreamDebugChatSender(private val printStream: PrintStream) : DebugChatSender {

    override fun shouldFilter() = false

    override fun sendDebug(message: IChatComponent) {
        printStream.println(message.unformattedText)
    }
}

class LoggerDebugChatSender(private val logger: Logger) : DebugChatSender {
    override fun sendDebug(message: IChatComponent) {
        logger.info("[DEBUG] ${message.formattedText}")
    }
}

class PlayerDebugChatSender(private val player: EntityPlayerSP) : DebugChatSender {
    override fun sendDebug(message: IChatComponent) {
        player.addChatComponentMessage(ChatComponentText("§6[§3Debug§6]§r ").appendSibling(message))
    }
}