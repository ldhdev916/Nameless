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

package com.happyandjust.nameless

import com.happyandjust.nameless.commands.DevCommand
import com.happyandjust.nameless.commands.FairySoulProfileCommand
import com.happyandjust.nameless.commands.HypixelCommand
import com.happyandjust.nameless.commands.TextureCommand
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.keybinding.NamelessKeyBinding
import com.happyandjust.nameless.listener.BasicListener
import com.happyandjust.nameless.listener.FeatureListener
import com.happyandjust.nameless.listener.LocrawListener
import com.happyandjust.nameless.listener.OutlineHandleListener
import com.happyandjust.nameless.mixins.accessors.AccessorMinecraft
import com.happyandjust.nameless.textureoverlay.OverlayResourcePack
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.command.CommandBase
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.ProgressManager
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION)
class Nameless {

    companion object {
        @Mod.Instance
        lateinit var INSTANCE: Nameless
    }

    val keyBindings = hashMapOf<KeyBindingCategory, NamelessKeyBinding>().apply {
        for (category in KeyBindingCategory.values()) {
            this[category] =
                NamelessKeyBinding(category.desc, category.key).also { ClientRegistry.registerKeyBinding(it) }
        }
    }
    lateinit var outlineHandleListener: OutlineHandleListener
    lateinit var locrawListener: LocrawListener

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {

        val progressBar = ProgressManager.push("Nameless", 3)

        if (!mc.framebuffer.isStencilEnabled) {
            mc.framebuffer.enableStencil()
        }

        progressBar.step("Fetching Requiring Data...")

        FeatureRegistry.MURDERER_FINDER.fetchAssassinData()

        FeatureRegistry.GTB_HELPER.fetchWordsData()

        SkyblockUtils.fetchSkyBlockData()

        progressBar.step("Adding Custom Resource Pack...")

        (mc as AccessorMinecraft).defaultResourcePacks.add(OverlayResourcePack())
        mc.refreshResources()

        progressBar.step("Registering Command & Events...")

        outlineHandleListener = OutlineHandleListener()
        locrawListener = LocrawListener()

        registerCommands(
            DevCommand(),
            HypixelCommand(),
            TextureCommand(),
            FairySoulProfileCommand(),
        )
        registerListeners(FeatureListener(), BasicListener(), outlineHandleListener, locrawListener)

        ProgressManager.pop(progressBar)
    }


    private fun registerCommands(vararg commands: CommandBase) {
        ClientCommandHandler.instance.apply {
            commands.forEach { registerCommand(it) }
        }
    }

    private fun registerListeners(vararg listeners: Any) {
        for (listener in listeners) {
            MinecraftForge.EVENT_BUS.register(listener)
        }
    }

}

const val MOD_ID = "nameless"
const val MOD_NAME = "Nameless"
const val VERSION = "1.0.0"
