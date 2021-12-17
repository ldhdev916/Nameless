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

import com.google.gson.JsonObject
import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.VERSION
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.core.Request
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.gui.UpdateGui
import gg.essential.api.utils.GuiUtil
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiMainMenu
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion
import java.io.File
import java.net.URI
import java.net.URL

object FeatureUpdateChecker : SimpleFeature(
    Category.MISCELLANEOUS,
    "updatechecker",
    "Auto Update Checker",
    "Automatically checks if currently loaded mod version is latest version",
    true
) {

    private var updateGui: () -> UpdateGui? = { null }
    private var shown = false
    private var needUpdate = false
    private var shouldShow = false

    @OptIn(DelicateCoroutinesApi::class)
    fun checkForUpdate() {
        if (!enabled) return

        GlobalScope.launch {
            val json =
                JsonHandler(Request.get("https://api.github.com/repos/HappyAndJust/Nameless/releases/latest"))
                    .read(JsonObject())
            val latestTag = json["tag_name"].asString.drop(1)

            val currentVersion = DefaultArtifactVersion(VERSION)
            val latestVersion = DefaultArtifactVersion(latestTag)


            if (currentVersion < latestVersion) {
                val html_url = json["html_url"].asString
                val assets = json["assets"].asJsonArray[0].asJsonObject

                val download_url = assets["browser_download_url"].asString
                val body = json["body"].asString

                updateGui = {
                    UpdateGui(body).apply {
                        htmlURL = URI(html_url)
                        downloadURL = URL(download_url)
                        jarFile = File(Nameless.modFile.parentFile, assets["name"].asString)
                    }
                }
                needUpdate = true
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