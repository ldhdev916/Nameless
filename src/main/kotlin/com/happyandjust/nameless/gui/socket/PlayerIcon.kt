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

package com.happyandjust.nameless.gui.socket

import com.happyandjust.nameless.dsl.fetch
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ImageAspectConstraint
import gg.essential.elementa.constraints.MousePositionConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.imageio.ImageIO

class PlayerIcon(playerName: String) : UIContainer() {

    init {
        val async = CompletableFuture.supplyAsync {
            val uuid =
                Json.decodeFromString<Name2UUID>("https://api.mojang.com/users/profiles/minecraft/$playerName".fetch()).id
            val profile =
                Json.decodeFromString<MojangProfile>("https://sessionserver.mojang.com/session/minecraft/profile/$uuid".fetch())
            val textureProperty = profile.properties.single { it.name == "textures" }
            val value =
                Json.decodeFromString<PropertyValue>(Base64.getDecoder().decode(textureProperty.value).decodeToString())
            val skinUrl = value.textures["SKIN"]!!["url"]!!.jsonPrimitive.content

            ImageIO.read(URL(skinUrl)).getSubimage(8, 8, 8, 8)
        }

        val nameText = UIText(playerName).constrain {
            x = MousePositionConstraint()
            y = MousePositionConstraint()
        }

        UIImage(async).constrain {
            width = 100.percent()
            height = ImageAspectConstraint()
        }.onMouseEnter {
            Window.enqueueRenderOperation {
                nameText childOf Window.of(this@PlayerIcon)
            }
        }.onMouseLeave {
           Window.enqueueRenderOperation {
               Window.of(this@PlayerIcon).removeChild(nameText)
           }
        } childOf this
    }

    @kotlinx.serialization.Serializable
    private data class Name2UUID(val name: String, val id: String)

    @kotlinx.serialization.Serializable
    private data class MojangProfile(val id: String, val name: String, val properties: List<ProfileProperty>)

    @kotlinx.serialization.Serializable
    private data class ProfileProperty(val name: String, val value: String)

    @kotlinx.serialization.Serializable
    private data class PropertyValue(
        val timestamp: Long,
        val profileId: String,
        val profileName: String,
        val textures: Map<String, JsonObject>
    )
}