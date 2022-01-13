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

import com.happyandjust.nameless.commands.*
import com.happyandjust.nameless.config.ConfigHandler
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.enums.OutlineMode
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.features.impl.misc.FeatureUpdateChecker
import com.happyandjust.nameless.features.impl.qol.FeatureAutoRequeue
import com.happyandjust.nameless.features.impl.qol.FeatureGTBHelper
import com.happyandjust.nameless.features.impl.qol.FeatureMurdererFinder
import com.happyandjust.nameless.features.impl.qol.FeaturePlayTabComplete
import com.happyandjust.nameless.features.impl.skyblock.FeatureEquipPetSkin
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.keybinding.NamelessKeyBinding
import com.happyandjust.nameless.listener.BasicListener
import com.happyandjust.nameless.listener.LocrawListener
import com.happyandjust.nameless.listener.OutlineHandleListener
import com.happyandjust.nameless.listener.WaypointListener
import com.happyandjust.nameless.serialization.converters.getEnumConverter
import com.happyandjust.nameless.utils.SkyblockUtils
import gg.essential.api.commands.Command
import kotlinx.coroutines.*
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import java.io.File

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION, modLanguageAdapter = "gg.essential.api.utils.KotlinAdapter")
object Nameless {

    val keyBindings = KeyBindingCategory.values()
        .associateWith { NamelessKeyBinding(it.desc, it.key).also(ClientRegistry::registerKeyBinding) }

    var selectedOutlineMode by ConfigValue(
        "outline",
        "selected",
        OutlineMode.OUTLINE,
        getEnumConverter()
    )

    lateinit var modFile: File

    @Mod.EventHandler
    fun preInit(e: FMLPreInitializationEvent) {
        modFile = e.sourceFile
        ConfigHandler.file = File(e.modConfigurationDirectory, "Nameless.json")
        FeatureUpdateChecker.checkForUpdate()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Mod.EventHandler
    fun init(e: FMLInitializationEvent) {
        if (!mc.framebuffer.isStencilEnabled) {
            mc.framebuffer.enableStencil()
        }

        val scope = CoroutineScope(Dispatchers.Default)

        val job = scope.launch { // init async as it takes long (KReflection)
            FeatureRegistry
        }

        scope.launch(Dispatchers.IO) {
            job.join()

            FeatureAutoRequeue.isAutoGGLoaded = Loader.isModLoaded("autogg")

            async { FeatureAutoRequeue.fetchGameEndData() }

            async { FeatureMurdererFinder.fetchAssassinData() }

            async { FeatureGTBHelper.fetchWordsData() }

            async { FeaturePlayTabComplete.fetchGameDataList() }

            async { FeatureEquipPetSkin.fetchPetSkinData() }

            async { SkyblockUtils.fetchSkyBlockData() }
        }

        registerCommands(
            DevCommand,
            HypixelCommand,
            SearchBinCommand,
            NameHistoryCommand,
            WaypointCommand,
            ViewStatCommand,
            ChangeHelmetTextureCommand,
            ShortCommand,
            PathFindCommand,
            GraphCommand
        )

        BasicListener
        LocrawListener
        OutlineHandleListener
        WaypointListener
    }


    private fun registerCommands(vararg commands: Command) {
        commands.forEach(Command::register)
    }
}

const val MOD_ID = "nameless"
const val MOD_NAME = "Nameless"
const val VERSION = "1.0.4"