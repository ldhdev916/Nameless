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

import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.dummySerializer
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.base.*
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.fixed
import gg.essential.elementa.ElementaVersion
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
import kotlin.random.Random

object TextureOverlay : SimpleFeature(
    "textureOverlay",
    "Texture Overlay",
    "Render texture which is under config/NamelessTextureOverlay to your screen. If you want to remove/add texture In Game, after modifying textures reload textures"
) {

    private val dir = File("config/NamelessTextureOverlay").apply { mkdirs() }

    init {
        val callback = {
            reloadTexture()
            mc.displayGuiScreen(null)
        }

        val callbackParameter = parameter(callback, dummySerializer()) {
            key = List(100) { if (Random.nextBoolean()) 'I' else 'l' }.joinToString("")
            title = "Reload Textures"

            settings {
                placeHolder = "Reload"
            }
        }

        hierarchy { +callbackParameter }

        reloadTexture()
    }


    private fun reloadTexture() {
        executeHierarchy {
            parameters.values.filterIsInstance<OverlayParameter<Boolean>>().forEach {
                -it
                it.value = false
            }
        }

        val imageFiles = dir.listFiles().orEmpty().filter {
            Files.probeContentType(it.toPath()).substringBefore("/") == "image"
        }
        for (file in imageFiles) {
            val name = file.name

            val parameter = overlayParameter(false) {
                key = name
                title = name

                val image = ImageIO.read(file)
                val window = Window(ElementaVersion.V1).apply {
                    UIImage(CompletableFuture.supplyAsync { image }).constrain {
                        x = basicXConstraint { overlayPoint.x.toFloat() }.fixed()
                        y = basicYConstraint { overlayPoint.y.toFloat() }.fixed()

                        width = basicWidthConstraint { image.width * overlayPoint.scale.toFloat() }.fixed()
                        height = ImageAspectConstraint()
                    } childOf this
                }

                wheel = max(image.width, image.height) / 5

                config("textureOverlay", "${name}_position", Overlay.DEFAULT)
                component {
                    UIImage(CompletableFuture.supplyAsync { image }).constrain {
                        width = basicWidthConstraint { image.width * currentScale.toFloat() }.fixed()
                        height = ImageAspectConstraint()
                    }
                }

                shouldDisplay { enabled && value }

                render { if (enabled && value) window.draw(UMatrixStack.Compat.get()) }
            }

            executeHierarchy {
                +parameter
            }
        }
    }
}