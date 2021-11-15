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

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.devqol.decodeBase64
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.sendPrefixMessage
import com.happyandjust.nameless.features.impl.skyblock.FeatureChangeHelmetTexture
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import com.happyandjust.nameless.utils.APIUtils
import com.happyandjust.nameless.utils.SkyblockUtils
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ImageAspectConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.universal.GuiScale
import net.minecraft.client.gui.GuiScreen
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.net.URL

object ChangeHelmetTextureCommand : ClientCommandBase("helmettexture") {

    lateinit var gui: () -> GuiScreen
    private val gson = Gson()

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            sendPrefixMessage("§cUsage: /helmettexture [SkyBlock ID] or /helmettexture view (SkyBlock ID)")
            return
        }

        if (args[0] == "view") {
            val skyBlockItem = if (args.size == 2) {
                try {
                    getSkyBlockItemByID(args[1].uppercase())
                } catch (e: IllegalArgumentException) {
                    sendPrefixMessage(e.message)
                    return
                }
            } else FeatureChangeHelmetTexture.currentlyEquipedTexture?.first ?: run {
                sendPrefixMessage("§cNo Currently selected texture")
                return
            }
            gui = {
                object :
                    WindowScreen(drawDefaultBackground = false, newGuiScale = GuiScale.scaleForScreenSize().ordinal) {

                    init {
                        val url = URL(
                            APIUtils.getSkinURLFromJSON(
                                gson.fromJson(
                                    skyBlockItem.skin.decodeBase64(),
                                    JsonObject::class.java
                                )
                            )
                        )

                        UIImage.ofURL(url).constrain {
                            x = CenterConstraint()
                            y = CenterConstraint()

                            width = 50.percent()
                            height = ImageAspectConstraint()
                        } childOf window
                    }
                }
            }
            MinecraftForge.EVENT_BUS.register(this)
        } else {
            val skyBlockItem = try {
                getSkyBlockItemByID(args[0].uppercase())
            } catch (e: IllegalArgumentException) {
                sendPrefixMessage(e.message)
                return
            }
            FeatureChangeHelmetTexture.setCurrentHelmetTexture(skyBlockItem)
            sendPrefixMessage("§aChanged Helmet Texture to ${skyBlockItem.id}")
        }
    }

    private fun getSkyBlockItemByID(id: String): SkyBlockItem {
        val skyBlockItem =
            SkyblockUtils.getItemFromId(id) ?: throw IllegalArgumentException("§cNo Such SkyBlock ID: $id")
        if (skyBlockItem.skin.isBlank()) {
            throw IllegalArgumentException("§c${skyBlockItem.id} doesn't have skin")
        }
        return skyBlockItem
    }

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos?
    ): MutableList<String> {
        return when (args.size) {
            1 -> filter(args[0])
            2 -> if (args[0] == "view") filter(args[1]) else super.addTabCompletionOptions(sender, args, pos)
            else -> super.addTabCompletionOptions(sender, args, pos)
        }
    }

    private fun filter(arg: String) =
        SkyblockUtils.allItems.values.filter { it.skin.isNotBlank() }.filter { it.id.contains(arg, true) }.map { it.id }
            .toMutableList()

    @SubscribeEvent
    fun onRenderTick(e: TickEvent.RenderTickEvent) {
        mc.displayGuiScreen(gui())
        MinecraftForge.EVENT_BUS.unregister(this)
    }
}