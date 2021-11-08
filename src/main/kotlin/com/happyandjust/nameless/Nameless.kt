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

import com.google.gson.JsonObject
import com.happyandjust.nameless.commands.*
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.core.OutlineMode
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.impl.misc.FeatureUpdateChecker
import com.happyandjust.nameless.features.impl.qol.FeatureGTBHelper
import com.happyandjust.nameless.features.impl.qol.FeatureMurdererFinder
import com.happyandjust.nameless.features.impl.qol.FeaturePlayTabComplete
import com.happyandjust.nameless.gui.GuiError
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.keybinding.NamelessKeyBinding
import com.happyandjust.nameless.listener.*
import com.happyandjust.nameless.mixins.accessors.AccessorMinecraft
import com.happyandjust.nameless.network.Request
import com.happyandjust.nameless.resourcepack.OverlayResourcePack
import com.happyandjust.nameless.resourcepack.SkinResourcePack
import com.happyandjust.nameless.serialization.converters.COutlineMode
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.command.CommandBase
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.logging.log4j.LogManager
import java.io.File
import java.util.concurrent.Executors

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION)
class Nameless {

    companion object {
        @Mod.Instance(MOD_ID)
        lateinit var INSTANCE: Nameless
    }

    val keyBindings = hashMapOf<KeyBindingCategory, NamelessKeyBinding>().apply {
        for (category in KeyBindingCategory.values()) {
            this[category] =
                NamelessKeyBinding(category.desc, category.key).also { ClientRegistry.registerKeyBinding(it) }
        }
    }
    private val selectedOutlineModeConfig = ConfigValue(
        "outline",
        "selected",
        OutlineMode.OUTLINE,
        COutlineMode
    )
    var selectedOutlineMode = OutlineMode.OUTLINE
        get() = selectedOutlineModeConfig.value
        set(value) {
            field = value

            selectedOutlineModeConfig.value = value
        }
    lateinit var modFile: File
    var isErrored = false
    private var shownErrorScreen = false
    private lateinit var reason: String

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onGuiOpen(e: GuiOpenEvent) {
        if (!isErrored) return
        if (shownErrorScreen) return
        shownErrorScreen = true
        val gui = e.gui

        if (gui is GuiMainMenu) {
            e.gui = GuiError(gui, reason)
        }
    }

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        modFile = e.sourceFile

        val errorVersions =
            JSONHandler(Request.get("https://raw.githubusercontent.com/HappyAndJust/Nameless/master/errorModVersions.json")).read(
                JsonObject()
            )

        for ((errorVersion, reason) in errorVersions.entrySet()) {
            if (errorVersion == VERSION) {

                isErrored = true
                this.reason = reason.asString

                LogManager.getLogger().fatal("[Nameless] Current Mod Version $VERSION is errored")

                break
            }
        }

        if (!isErrored) {
            FeatureUpdateChecker.checkForUpdate()
        }
    }

    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        if (isErrored) {
            MinecraftForge.EVENT_BUS.register(this)
            return
        }

        val threadPool = Executors.newFixedThreadPool(2)

        if (!mc.framebuffer.isStencilEnabled) {
            mc.framebuffer.enableStencil()
        }


        threadPool.execute {
            FeatureMurdererFinder.fetchAssassinData()

            FeatureGTBHelper.fetchWordsData()

            FeaturePlayTabComplete.fetchGameDataList()
        }

        threadPool.execute {
            SkyblockUtils.fetchSkyBlockData()
        }


        (mc as AccessorMinecraft).defaultResourcePacks.add(OverlayResourcePack)
        (mc as AccessorMinecraft).defaultResourcePacks.add(SkinResourcePack)
        mc.refreshResources()

        registerCommands(
            DevCommand,
            HypixelCommand,
            TextureCommand,
            SearchBinCommand,
            NameHistoryCommand,
            WaypointCommand
        )
        registerListeners(FeatureListener, BasicListener, OutlineHandleListener, LocrawListener, WaypointListener)
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
const val VERSION = "1.0.3"
