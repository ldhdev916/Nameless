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

package com.happyandjust.nameless.features.impl.misc

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.happyandjust.nameless.MOD_NAME
import com.happyandjust.nameless.VERSION
import com.happyandjust.nameless.commands.ShutDownCommand
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.devqol.LOGGER
import com.happyandjust.nameless.devqol.getMD5
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.WorldLoadListener
import com.happyandjust.nameless.network.Request
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion

object FeatureUpdateChecker : SimpleFeature(
    Category.MISCELLANEOUS,
    "updatechecker",
    "Auto Update Checker",
    "automatically checks if currently loaded mod version is latest version",
    true
), WorldLoadListener, ClientTickListener {

    private var checkedVersion = false
    var needUpdate = false
    private var scheduledMessage: IChatComponent? = null


    override fun onWorldLoad(e: WorldEvent.Load) {
        if (checkedVersion || !enabled) return
        checkedVersion = true
        threadPool.execute {
            val s = try {
                Request.get("https://api.github.com/repos/HappyAndJust/Nameless/releases/latest")
            } catch (e: Exception) {
                scheduledMessage = ChatComponentText("§c[Nameless] Unable to check latest version.")
                LOGGER.error(e)
                return@execute
            }
            val jsonObject = JSONHandler(s).read(JsonObject())

            val latestTag = jsonObject["tag_name"].asString.substring(1) // v1.0.0
            val html_url = jsonObject["html_url"].asString

            val asset = (jsonObject["assets"] as JsonArray)[0].asJsonObject

            val download_url = asset["browser_download_url"].asString

            ShutDownCommand.downloadURL = download_url
            ShutDownCommand.jarName = asset["name"].asString

            val currentVersion = DefaultArtifactVersion(VERSION)
            val latestVersion = DefaultArtifactVersion(latestTag)

            if (currentVersion < latestVersion) { // do update

                needUpdate = true

                val openGithub = ChatComponentText("§a§l[Open Github]").apply {
                    chatStyle =
                        chatStyle.setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, html_url))
                }
                val download = ChatComponentText("§a§l[Download]").apply {
                    chatStyle = chatStyle.setChatClickEvent(
                        ClickEvent(
                            ClickEvent.Action.OPEN_URL,
                            download_url
                        )
                    )
                }

                val auto_download = ChatComponentText("§6§l[Auto Update]").apply {
                    chatStyle = chatStyle.setChatClickEvent(
                        ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "autoupdateshutdown ${"auto-update".getMD5()}"
                        )
                    )
                }

                val chat =
                    ChatComponentText("§c§l$MOD_NAME is outdated. Please update to $latestTag.\n")
                        .appendSibling(openGithub)
                        .appendText(" ")
                        .appendSibling(download)
                        .appendText(" ")
                        .appendSibling(auto_download)

                scheduledMessage = chat

            } else if (currentVersion > latestVersion) { // pre
                scheduledMessage = ChatComponentText(
                    """
                        §9§lYou're in Pre-Version of $MOD_NAME
                        §9§lCurrent Mod Version: $VERSION Latest Version: $latestTag
                    """.trimIndent()
                )
            }
        }
    }

    override fun tick() {
        scheduledMessage?.let {
            sendClientMessage(it)
            scheduledMessage = null
            return
        }
    }
}
