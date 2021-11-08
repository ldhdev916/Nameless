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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.gui.relocate.RelocateGui
import com.happyandjust.nameless.resourcepack.OverlayResourcePack
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.ImageAspectConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import net.minecraft.client.gui.Gui
import net.minecraft.util.ResourceLocation
import java.io.File
import javax.imageio.ImageIO

object FeatureTextureOverlay : SimpleFeature(
    Category.MISCELLANEOUS,
    "textureoverlay",
    "Texture Overlay",
    "Render texture which is under config/NamelessTextureOverlay to your screen. If you want to remove/add texture In Game, after modifying textures type /reloadtexture"
), RenderOverlayListener {

    init {
        reloadTexture()
    }

    fun reloadTexture() {
        parameters.clear()

        for (file in OverlayResourcePack.dir.listFiles() ?: emptyArray()) {
            val name = file.name

            if (name.endsWith(".png") || name.endsWith(".jpg")) {
                parameters[name] = object : OverlayParameter<Boolean>(
                    0,
                    "textureoverlay",
                    name,
                    name,
                    "",
                    false,
                    CBoolean
                ) {

                    private val resourceLocation = ResourceLocation("namelesstextureoverlay", name)
                    private val image = ImageIO.read(file)

                    override val overlayPoint =
                        ConfigValue("textureoverlay", "${name}_position", Overlay.DEFAULT, COverlay)

                    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
                        return UIImage.ofFile(File(OverlayResourcePack.dir, name)).constrain {
                            width = (image.width * relocateComponent.currentScale).pixels()
                            height = ImageAspectConstraint()

                            relocateComponent.onScaleChange {
                                width = (image.width * it).pixels()
                            }
                        }
                    }

                    override fun renderOverlay(partialTicks: Float) {
                        val overlay = overlayPoint.value

                        mc.textureManager.bindTexture(resourceLocation)

                        val width = (image.width * overlay.scale).toInt()
                        val height = (image.height * overlay.scale).toInt()

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
                    }

                    override fun getWheelSensitive() = 40

                }
            }
        }
    }

    override fun renderOverlay(partialTicks: Float) {
        if (!enabled) return
        if (mc.currentScreen is RelocateGui) return

        for (parameter in parameters.values.filterIsInstance<OverlayParameter<Boolean>>()) {
            if (parameter.value) {
                parameter.renderOverlay(partialTicks)
            }
        }
    }

    data class OverlayInfo(val resourceLocation: ResourceLocation, val width: Int, val height: Int)
}