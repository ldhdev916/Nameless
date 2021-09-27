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

package com.happyandjust.nameless.textureoverlay

import net.minecraft.client.resources.IResourcePack
import net.minecraft.client.resources.data.IMetadataSection
import net.minecraft.client.resources.data.IMetadataSerializer
import net.minecraft.util.ResourceLocation
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream

class OverlayResourcePack : IResourcePack {

    private val dir = File("config/NamelessTextureOverlay").also { it.mkdirs() }

    override fun getInputStream(location: ResourceLocation): InputStream {
        return File(dir, location.resourcePath).inputStream().buffered()
    }

    override fun resourceExists(location: ResourceLocation): Boolean {
        return File(dir, location.resourcePath).isFile
    }

    override fun getResourceDomains(): MutableSet<String> = hashSetOf("namelesstextureoverlay")

    override fun <T : IMetadataSection?> getPackMetadata(
        metadataSerializer: IMetadataSerializer?,
        metadataSectionName: String?
    ): T? {
        return null
    }

    override fun getPackImage(): BufferedImage = BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB)

    override fun getPackName() = "NamelessTextureOverlay"
}