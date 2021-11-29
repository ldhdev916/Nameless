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
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.features.impl.skyblock.FeatureChangeHelmetTexture
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object ChangeHelmetTextureCommand : ClientCommandBase("helmettexture") {
    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            sendUsage("[SkyBlock ID]")
            return
        }

        val skyBlockItem = try {
            getSkyBlockItemByID(args[0].uppercase())
        } catch (e: IllegalArgumentException) {
            sendPrefixMessage(e.message)
            return
        }
        FeatureChangeHelmetTexture.setCurrentHelmetTexture(skyBlockItem)
        sendPrefixMessage("§aChanged Helmet Texture to ${skyBlockItem.id}")

    }

    private fun getSkyBlockItemByID(id: String): SkyBlockItem {
        val skyBlockItem =
            SkyblockUtils.getItemFromId(id) ?: throw IllegalArgumentException("§cNo Such SkyBlock ID: $id")
        return if (skyBlockItem.skin.isBlank()) {
            throw IllegalArgumentException("§c${skyBlockItem.id} doesn't have skin")
        } else skyBlockItem
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos?
    ): MutableList<String> {
        return when (args.size) {
            1 -> SkyblockUtils.allItems.values.filter { it.skin.isNotBlank() }.filter { it.id.contains(args[0], true) }
                .map { it.id }
                .toMutableList()
            else -> super.addTabCompletionOptions(sender, args, pos)
        }
    }

}