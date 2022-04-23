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

package com.happyandjust.nameless.features.impl.misc

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.VERSION
import com.happyandjust.nameless.dsl.fetch
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.string
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.impl.qol.JoinHypixelImmediately
import com.happyandjust.nameless.gui.UpdateGui
import gg.essential.api.utils.GuiUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import java.io.File
import java.net.URI
import java.net.URL

object UpdateChecker : SimpleFeature(
    "updateChecker",
    "Auto Update Checker",
    "Automatically checks if currently loaded mod version is latest version",
    true
) {

    private var updateGui: () -> UpdateGui? = { null }
    private var shown = false
    private var needUpdate = false
    private var shouldShow = false

    fun checkForUpdate() {
        if (!enabled) return

        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject =
                Json.decodeFromString<JsonObject>("https://api.github.com/repos/HappyAndJust/Nameless/releases/latest".fetch())
            val latestTag = jsonObject["tag_name"]!!.string.drop(1)

            val fixVersion = VERSION.substringBefore("-Pre")

            val currentVersion = DefaultArtifactVersion(fixVersion)
            val latestVersion = DefaultArtifactVersion(latestTag)

            if (currentVersion < latestVersion || (fixVersion == latestTag && "Pre" in VERSION)) {
                val htmlUrl = jsonObject["html_url"]!!.string
                val assets = jsonObject["assets"]!!.jsonArray[0].jsonObject

                val downloadUrl = assets["browser_download_url"]!!.string
                val body = jsonObject["body"]!!.string

                updateGui = {
                    UpdateGui(body).apply {
                        htmlURL = URI(htmlUrl)
                        downloadURL = URL(downloadUrl)
                        jarFile = File(Nameless.modFile.parentFile, assets["name"]!!.string)
                    }
                }
                needUpdate = true
                JoinHypixelImmediately.stop = true
            }
        }
    }

    init {
        on<GuiOpenEvent>().filter { gui is GuiMainMenu && enabled && needUpdate && !shown }.subscribe {
            shouldShow = true
        }

        on<TickEvent.ClientTickEvent>().filter { phase == TickEvent.Phase.END && shouldShow && !shown && enabled }
            .subscribe {
                shown = true
                shouldShow = false

                GuiUtil.open(updateGui() ?: return@subscribe)
            }
    }

}