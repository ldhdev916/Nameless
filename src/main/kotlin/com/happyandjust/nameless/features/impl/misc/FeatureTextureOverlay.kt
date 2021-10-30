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

import com.happyandjust.nameless.core.Point
import com.happyandjust.nameless.devqol.color
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.serialization.converters.COverlay
import com.happyandjust.nameless.textureoverlay.ERelocateGui
import com.happyandjust.nameless.textureoverlay.Overlay
import com.happyandjust.nameless.textureoverlay.impl.ETextureOverlay
import net.minecraft.client.gui.Gui
import net.minecraft.util.ResourceLocation
import java.io.File
import javax.imageio.ImageIO

object FeatureTextureOverlay : SimpleFeature(
    Category.MISCELLANEOUS,
    "textureoverlay",
    "Texture Overlay",
    "Render texture which is under config/NamelessTextureOverlay to your screen\nIf you want to remove/add texture InGame, after modifying textures type /reloadtexture"
), RenderOverlayListener {

    private val textureCache = hashMapOf<String, OverlayInfo>()

    init {
        reloadTexture()
    }

    fun reloadTexture() {

        textureCache.clear()
        parameters.clear()

        val dir = File("config/NamelessTextureOverlay/")

        for (file in dir.listFiles() ?: emptyArray()) {
            val name = file.name

            if (name.endsWith(".png") || name.endsWith(".jpg")) {
                parameters[name] = FeatureParameter(
                    0,
                    "textureoverlay",
                    name,
                    name,
                    "",
                    Overlay(Point(0, 0), 1.0),
                    COverlay
                ).also {
                    it.relocateGui = {
                        val textureOverlay = ETextureOverlay(it.value, textureCache[name]!!)

                        ERelocateGui(
                            textureOverlay
                        ) { overlay -> it.value = overlay }
                    }
                }

                val image = ImageIO.read(file)

                textureCache[name] =
                    OverlayInfo(ResourceLocation("namelesstextureoverlay", name), image.width, image.height)
            }
        }
    }

    override fun renderOverlay(partialTicks: Float) {
        if (!enabled) return
        if (mc.currentScreen is ERelocateGui) return

        for ((name, overlayInfo) in textureCache) {
            val overlay = getParameterValue<Overlay>(name)

            mc.textureManager.bindTexture(overlayInfo.resourceLocation)

            val width = (overlayInfo.width * overlay.scale).toInt()
            val height = (overlayInfo.height * overlay.scale).toInt()

            Gui.drawModalRectWithCustomSizedTexture(
                overlay.point.x,
                overlay.point.y,
                0f,
                0f,
                width,
                height,
                width.toFloat(),
                height.toFloat()
            )

            color(1f, 1f, 1f, 1f)
        }
    }

    data class OverlayInfo(val resourceLocation: ResourceLocation, val width: Int, val height: Int)
}