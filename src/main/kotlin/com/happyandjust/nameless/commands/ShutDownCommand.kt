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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.devqol.downloadToFile
import com.happyandjust.nameless.devqol.getMD5
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.features.impl.misc.FeatureUpdateChecker
import com.happyandjust.nameless.utils.Utils
import net.minecraft.command.ICommandSender
import net.minecraftforge.fml.common.FMLCommonHandler
import java.io.File
import java.net.URL
import kotlin.concurrent.thread

object ShutDownCommand : ClientCommandBase("autoupdateshutdown") {

    var downloadURL = ""
    var jarName = ""

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {

        if (args.size != 1) return

        if (args[0] != "auto-update".getMD5()) return

        if (downloadURL.isEmpty() || jarName.isEmpty()) {
            sendClientMessage("§cDownload Information is not enough to auto-download")
            return
        }

        if (!FeatureUpdateChecker.needUpdate) {
            sendClientMessage("§cYou don't need update")
            return
        }

        try {
            Runtime.getRuntime().addShutdownHook(thread(false) {
                try {
                    val dir = Nameless.INSTANCE.modFile.parentFile
                    val configDir = File("config/HappyAndJust/")

                    configDir.mkdirs()

                    URL(downloadURL).downloadToFile(File(dir, jarName))

                    URL("https://github.com/HappyAndJust/Deleter/releases/download/v1.0/Deleter.jar").downloadToFile(
                        File(configDir, "Deleter.jar")
                    )

                    Utils.deleteOldJar()

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

            FMLCommonHandler.instance().exitJava(0, false)

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}