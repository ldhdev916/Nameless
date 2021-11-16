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

import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.gui.feature.FeatureGui
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CString
import com.happyandjust.nameless.utils.APIUtils
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.util.ResourceLocation
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import java.net.URL
import java.util.concurrent.Callable
import javax.imageio.ImageIO


object FeatureDisguiseNickname : SimpleFeature(
    Category.MISCELLANEOUS,
    "disguisenickname",
    "Disguise Nickname",
    "Change your nickname and skin if nickname is valid!"
) {

    private val dir = File("config/NamelessSkinTextures").also { it.mkdirs() }

    init {
        parameters["nick"] = FeatureParameter(
            0,
            "disguise",
            "nickname",
            "Disguise Nickname",
            "If you leave this empty, your nickname will disappear",
            "",
            CString
        ).also {
            it.validator = { char -> char.isLetterOrDigit() }
        }
        parameters["skin"] = FeatureParameter(
            1,
            "disguise",
            "chnageskin",
            "Change Skin",
            "If nickname you set above is valid, your skin will be changed into his skin\nAs it caches skin texture by player's uuid, if player changes his skin it can show incorrect skin\nIn this case go directory ${dir.absolutePath} and delete all png files",
            false,
            CBoolean
        )
    }

    private val downloadingSkinUsernames = hashSetOf<String>()
    val cachedUsernameResourceLocation = hashMapOf<String, ResourceLocation>()
    private val invalidUsernames = hashSetOf<String>()

    fun getNickname() = getParameterValue<String>("nick")

    fun checkAndDownloadSkin(username: String) {
        if (mc.currentScreen is FeatureGui) return // in case you're writing username but mod stupidly gets all text you write
        if (downloadingSkinUsernames.contains(username)) return
        if (invalidUsernames.contains(username)) return
        if (cachedUsernameResourceLocation.contains(username)) return
        threadPool.execute {
            downloadingSkinUsernames.add(username)
            val uuid = try {
                APIUtils.getUUIDFromUsername(username)
            } catch (ignored: Exception) {
                invalidUsernames.add(username)

                downloadingSkinUsernames.remove(username)
                return@execute
            }

            val file = File(dir, "$uuid.png")

            val getResourceLocation: () -> ResourceLocation =
                {
                    mc.addScheduledTask(
                        Callable {
                            mc.textureManager.getDynamicTextureLocation(
                                file.name,
                                DynamicTexture(ImageIO.read(file))
                            )
                        }
                    ).get()
                }
            if (file.isFile) {
                cachedUsernameResourceLocation[username] = getResourceLocation()
                downloadingSkinUsernames.remove(username)
                return@execute
            }

            try {
                downloadSkin(uuid)

                Thread.sleep(2000L) // wait for image fully loaded
                cachedUsernameResourceLocation[username] = getResourceLocation()
            } finally {
                downloadingSkinUsernames.remove(username)
            }
        }
    }

    private fun downloadSkin(uuid: String) {
        threadPool.execute {
            try {
                var image = ImageIO.read(URL(APIUtils.getSkinURLFromUUID(uuid)))

                if (image.width == 64 && image.height == 32) {
                    image = convertImageInto64x64(image)
                }

                if (image.width != 64 || image.height != 64) {
                    sendClientMessage("§cDownloaded Image is not 64x64")
                    return@execute
                }

                val file = File(dir, "$uuid.png")

                ImageIO.write(image, "png", file)
                sendClientMessage("§aSuccessfully downloaded $uuid's skin")

            } catch (e: Exception) {
                e.printStackTrace()
                sendClientMessage("§cFailed to Download Skin from uuid: $uuid")
            }
        }
    }

    private fun convertImageInto64x64(skin: Image): BufferedImage {
        val upper = BufferedImage(64, 32, 2)
        upper.graphics.drawImage(skin, 0, 0, 64, 32, 0, 0, 64, 32, null)
        var armF = BufferedImage(12, 12, 2)
        armF.graphics.drawImage(skin, 0, 0, 12, 12, 40, 20, 52, 32, null)
        val tx = AffineTransform.getScaleInstance(-1.0, 1.0)
        tx.translate(-armF.getWidth(null).toDouble(), 0.0)
        val op = AffineTransformOp(tx, 1)
        armF = op.filter(armF, null)
        var armB = BufferedImage(4, 12, 2)
        armB.graphics.drawImage(skin, 0, 0, 4, 12, 52, 20, 56, 32, null)
        val txab = AffineTransform.getScaleInstance(-1.0, 1.0)
        txab.translate(-armB.getWidth(null).toDouble(), 0.0)
        val opab = AffineTransformOp(txab, 1)
        armB = opab.filter(armB, null)
        var armT = BufferedImage(4, 4, 2)
        armT.graphics.drawImage(skin, 0, 0, 4, 4, 44, 16, 48, 20, null)
        val txat = AffineTransform.getScaleInstance(-1.0, 1.0)
        txat.translate(-armT.getWidth(null).toDouble(), 0.0)
        val opat = AffineTransformOp(txat, 1)
        armT = opat.filter(armT, null)
        var armBo = BufferedImage(4, 4, 2)
        armBo.graphics.drawImage(skin, 0, 0, 4, 4, 48, 16, 52, 20, null)
        armBo = opat.filter(armBo, null)
        var legF = BufferedImage(12, 12, 2)
        legF.graphics.drawImage(skin, 0, 0, 12, 12, 0, 20, 12, 32, null)
        legF = op.filter(legF, null)
        var legB = BufferedImage(4, 12, 2)
        legB.graphics.drawImage(skin, 0, 0, 4, 12, 12, 20, 16, 32, null)
        legB = opab.filter(legB, null)
        var legT = BufferedImage(4, 4, 2)
        legT.graphics.drawImage(skin, 0, 0, 4, 4, 4, 16, 8, 20, null)
        legT = opat.filter(legT, null)
        var legBo = BufferedImage(4, 4, 2)
        legBo.graphics.drawImage(skin, 0, 0, 4, 4, 8, 16, 12, 20, null)
        legBo = opab.filter(legBo, null)
        val bi = BufferedImage(64, 64, 2)
        bi.graphics.drawImage(upper, 0, 0, 64, 64, 0, 0, 64, 64, null)
        bi.graphics.drawImage(armF, 0, 0, 64, 64, -32, -52, 32, 12, null)
        bi.graphics.drawImage(armB, 0, 0, 64, 64, -44, -52, 20, 12, null)
        bi.graphics.drawImage(armT, 0, 0, 64, 64, -36, -48, 28, 16, null)
        bi.graphics.drawImage(armBo, 0, 0, 64, 64, -40, -48, 24, 16, null)
        bi.graphics.drawImage(legF, 0, 0, 64, 64, -16, -52, 48, 12, null)
        bi.graphics.drawImage(legB, 0, 0, 64, 64, -28, -52, 36, 12, null)
        bi.graphics.drawImage(legT, 0, 0, 64, 64, -20, -48, 44, 16, null)
        bi.graphics.drawImage(legBo, 0, 0, 64, 64, -24, -48, 40, 16, null)
        return bi
    }
}