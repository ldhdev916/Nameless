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
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.serialization.DummyConverter
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.constraints.ImageAspectConstraint
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.io.File
import javax.imageio.ImageIO

object FeatureTextureOverlay : SimpleFeature(
    Category.MISCELLANEOUS,
    "textureoverlay",
    "Texture Overlay",
    "Render texture which is under config/NamelessTextureOverlay to your screen. If you want to remove/add texture In Game, after modifying textures reload textures"
), RenderOverlayListener {

    private val dir = File("config/NamelessTextureOverlay").also { it.mkdirs() }

    init {
        val callback = {
            reloadTexture()
            mc.displayGuiScreen(null)
        }
        parameters["reload"] = FeatureParameter(
            0,
            "textureoverlay",
            "reload",
            "Reload Textures",
            "",
            callback,
            DummyConverter(callback)
        ).also {
            it.placeHolder = "Reload"
        }

        reloadTexture()
    }

    fun reloadTexture() {
        parameters.entries.removeIf { it.value is OverlayParameter }

        for (file in dir.listFiles() ?: emptyArray()) {
            val name = file.name

            if (name.endsWith(".png") || name.endsWith(".jpg")) {
                parameters[name] = object : OverlayParameter<Boolean>(
                    1,
                    "textureoverlay",
                    name,
                    name,
                    "",
                    false,
                    CBoolean
                ) {

                    private val image = ImageIO.read(file)
                    private val resourceLocation =
                        mc.textureManager.getDynamicTextureLocation(file.name, DynamicTexture(image))

                    override val overlayPoint =
                        ConfigValue("textureoverlay", "${name}_position", Overlay.DEFAULT, COverlay)

                    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
                        return UIImage.ofFile(File(dir, name)).constrain {
                            width = (image.width * relocateComponent.currentScale).pixels()
                            height = ImageAspectConstraint()

                            relocateComponent.onScaleChange {
                                width = (image.width * it).pixels()
                            }
                        }
                    }

                    override fun shouldDisplayInRelocateGui(): Boolean {
                        return enabled && value
                    }

                    override fun renderOverlay0(partialTicks: Float) {
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

                    override fun getWheelSensitive() = 200

                }
            }
        }
    }

    override fun renderOverlay(partialTicks: Float) {
        if (!enabled) return
        for (parameter in parameters.values.filterIsInstance<OverlayParameter<Boolean>>()) {
            if (parameter.value) {
                parameter.renderOverlay(partialTicks)
            }
        }
    }

    data class OverlayInfo(val resourceLocation: ResourceLocation, val width: Int, val height: Int)
}