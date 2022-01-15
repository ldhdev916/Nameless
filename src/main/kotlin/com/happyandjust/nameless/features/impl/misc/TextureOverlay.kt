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
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.serialization.DummyConverter
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ImageAspectConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.UMatrixStack
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO
import kotlin.math.max

object TextureOverlay : SimpleFeature(
    Category.MISCELLANEOUS,
    "textureoverlay",
    "Texture Overlay",
    "Render texture which is under config/NamelessTextureOverlay to your screen. If you want to remove/add texture In Game, after modifying textures reload textures"
) {

    private val dir = File("config/NamelessTextureOverlay").apply { mkdirs() }
    private val matrixStack by lazy { UMatrixStack.Compat.get() }

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
            DummyConverter()
        ).apply {
            placeHolder = "Reload"
        }

        reloadTexture()
    }

    private fun reloadTexture() {
        parameters.values.removeIf { it is OverlayParameter }

        val files = (dir.listFiles() ?: emptyArray()).filter {
            Files.probeContentType(it.toPath()).split("/")[0] == "image"
        }

        for (file in files) {
            val name = file.name
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
                private val window = Window(ElementaVersion.V1)

                init {
                    UIImage(CompletableFuture.supplyAsync { image }).constrain {

                        x = basicXConstraint { overlayPoint.point.x.toFloat() }.fixed()
                        y = basicYConstraint { overlayPoint.point.y.toFloat() }.fixed()

                        width = basicWidthConstraint { image.width * overlayPoint.scale.toFloat() }.fixed()
                        height = ImageAspectConstraint()
                    } childOf window
                }

                override var overlayPoint by ConfigValue(
                    "textureoverlay",
                    "${name}_position",
                    Overlay.DEFAULT,
                    COverlay
                )

                override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
                    return UIImage(CompletableFuture.supplyAsync { image }).constrain {
                        width = basicWidthConstraint { image.width * relocateComponent.currentScale.toFloat() }.fixed()
                        height = ImageAspectConstraint()
                    }
                }

                override fun shouldDisplayInRelocateGui(): Boolean {
                    return enabled && value
                }

                override fun renderOverlay0(partialTicks: Float) {
                    if (!enabled || !value) return
                    window.draw(matrixStack)
                }

                override fun getWheelSensitive() = max(image.width, image.height) / 5

            }
        }
    }
}