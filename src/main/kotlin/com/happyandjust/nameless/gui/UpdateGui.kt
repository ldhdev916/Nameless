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

package com.happyandjust.nameless.gui

import com.happyandjust.nameless.dsl.formatDouble
import com.happyandjust.nameless.dsl.transformToPrecision
import com.happyandjust.nameless.gui.feature.ColorCache
import com.happyandjust.nameless.utils.Utils
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.markdown.MarkdownComponent
import gg.essential.universal.GuiScale
import net.minecraftforge.fml.common.FMLCommonHandler
import java.awt.Color
import java.awt.Desktop
import java.io.File
import java.net.URI
import java.net.URL
import kotlin.concurrent.thread

class UpdateGui(markDownText: String) : WindowScreen(
    newGuiScale = GuiScale.scaleForScreenSize().ordinal,
    restoreCurrentGuiOnClose = true
) {

    lateinit var jarFile: File
    lateinit var htmlURL: URI
    lateinit var downloadURL: URL

    private val markDownContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = 10.pixels()

        width = 45.percent()
        height = 75.percent()
    } childOf window

    private val scroller = ScrollComponent(customScissorBoundingBox = markDownContainer).constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf markDownContainer

    private val scrollBar = UIBlock(ColorCache.scrollBar).constrain {
        x = SiblingConstraint()
        width = 3.pixels()
    } childOf markDownContainer

    private val buttonContainer = UIContainer().constrain {
        width = 100.percent()

        y = SiblingConstraint()

        height = FillConstraint(false)
    } childOf window

    private val updateMessage = UIText("").constrain {
        x = CenterConstraint()
        y = CenterConstraint()

        textScale = 1.5.pixels()
    } childOf buttonContainer

    init {

        updateMessage.hide()
        scroller.setVerticalScrollBarComponent(scrollBar, true)

        MarkdownComponent(markDownText).constrain {
            x = CenterConstraint()

            width = 100.percent()
            height = 100.percent()
        } childOf scroller

        ActionButton("Update") {
            hide()
            updateMessage.unhide()
            doUpdate()

        }.constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            width = AspectConstraint(3f)
            height = 20.pixels()
        } childOf buttonContainer
    }

    private fun doUpdate() {
        thread {
            try {
                val configDir = File("config/HappyAndJust/")
                configDir.mkdirs()

                val deleterFile = File(configDir, "Deleter.jar")

                if (!deleterFile.exists()) {
                    downloadFile(
                        deleterFile,
                        URL("https://github.com/HappyAndJust/Deleter/releases/download/v1.0/Deleter.jar")
                    )
                }
            } catch (e: Exception) {
                updateMessage.setText("§cException Occurred while downloading old file deleter jar.")
                try {
                    Desktop.getDesktop().open(jarFile.parentFile)
                } catch (ignored: Exception) {

                }
                Thread.sleep(2000L)
            }

            try {
                downloadFile(jarFile, downloadURL)
            } catch (e: Exception) {
                updateMessage.setText("§cException Occurred while downloading latest mod file")
                try {
                    Desktop.getDesktop().browse(htmlURL)
                } catch (ignored: Exception) {

                }
            }

            Runtime.getRuntime().addShutdownHook(Thread {
                Utils.deleteOldJar()
            })

            FMLCommonHandler.instance().exitJava(-1, false)
        }
    }

    private fun downloadFile(file: File, url: URL) {

        val name = file.name

        updateMessage.setText("Downloading $name!")

        val inputStream = url.openStream().buffered()

        val bytes = inputStream.readBytes()

        inputStream.close()

        val length = bytes.sum()
        var count = 0

        val progressBox = ProgressBox().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()

            width = CopyConstraintFloat() boundTo updateMessage
            height = CopyConstraintFloat() boundTo updateMessage
        } childOf buttonContainer

        file.outputStream().buffered().use {

            for (byte in bytes) {

                count += byte
                it.write(byte.toInt())

                val dot = DOTS[((System.currentTimeMillis() % (DOTS.size * DOT_TIME)) / DOT_TIME).toInt()]
                updateMessage.setText("Downloading $name$dot")

                progressBox.currentProgress = count / length.toDouble()
            }


            Window.enqueueRenderOperation {
                progressBox.hide()
            }

            updateMessage.setText("§aDownload Complete!")

            it.flush()
        }
    }

    companion object {
        private val DOTS = arrayOf(".", "..", "...", "...", "...")
        private const val DOT_TIME = 200
    }

    class ProgressBox : UIContainer() {
        var currentProgress: Double = 0.0
            set(value) {
                field = value.coerceIn(0.0, 1.0)

                progressBar.constraints.width = (field * 100).percent()
                progressText.setText("${(field * 100).transformToPrecision(2).formatDouble()}%")
            }


        private val progressBar = UIBlock(ColorCache.accent).constrain {
            height = 100.percent()
        } childOf this

        private val progressText = UIText().constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf this

        init {
            effect(OutlineEffect(Color.lightGray, 1f))
        }
    }

}