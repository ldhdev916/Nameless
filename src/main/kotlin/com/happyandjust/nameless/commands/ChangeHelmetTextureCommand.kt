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

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.impl.skyblock.ChangeHelmetTexture
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import com.happyandjust.nameless.mixins.accessors.AccessorGuiChat
import com.happyandjust.nameless.utils.SkyblockUtils
import gg.essential.api.commands.Command
import gg.essential.api.commands.DefaultHandler
import gg.essential.api.commands.DisplayName
import net.minecraft.network.play.server.S3APacketTabComplete
import java.util.regex.Matcher

object ChangeHelmetTextureCommand : Command("helmettexture") {

    private val pattern = "/helmettexture (?<id>\\w*)".toPattern()

    @DefaultHandler
    fun handle(@DisplayName("SkyBlock ID") id: String) {
        val skyBlockItem = runCatching {
            getSkyBlockItemByID(id.uppercase())
        }.getOrElse {
            sendPrefixMessage(it.message)
            return
        }

        ChangeHelmetTexture.setCurrentHelmetTexture(skyBlockItem)
        sendPrefixMessage("§aChanged Helmet Texture to ${skyBlockItem.id}")
    }

    private fun getSkyBlockItemByID(id: String): SkyBlockItem {
        val skyBlockItem = SkyblockUtils.getItemFromId(id) ?: error("§cNo Such SkyBlock Item $id")
        return if (skyBlockItem.skin.isBlank()) {
            error("§c${skyBlockItem.id} doesn't have skin")
        } else skyBlockItem
    }

    init {
        on<PacketEvent.Received>().filter { packet is S3APacketTabComplete }.subscribe {
            mc.currentScreen.withInstance<AccessorGuiChat> {
                pattern.matchesMatcher(inputField.text) {
                    packet = S3APacketTabComplete(getTabCompletion())
                }
            }
        }
    }

    private fun Matcher.getTabCompletion() = SkyblockUtils.allItems.values.filter {
        it.skin.isNotBlank() && it.id.contains(group("id"), true)
    }.map { it.id }.toTypedArray()


}