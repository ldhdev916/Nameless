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

package com.happyandjust.nameless.features.impl.settings

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.dsl.matchesMatcher
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.pureText
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.features.SettingFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.PropertyData
import gg.essential.api.EssentialAPI
import net.minecraftforge.client.event.ClientChatReceivedEvent

object FeatureHypixelAPIKey : SettingFeature("hypixelapikey", "Hypixel API Key", "Your hypixel api key") {

    private val API_PATTERN = "Your new API key is (?<api>.+)".toPattern()
    var apiKey by ConfigValue.StringConfigValue("hypixel", "apikey", "")

    override fun getComponentType() = ComponentType.PASSWORD

    override fun getProperty() = ::apiKey

    override fun toPropertyData(): PropertyData<out Any?> {
        return super.toPropertyData().also { propertyData ->
            propertyData.validator = { it.isLowerCase() || it.isDigit() || it == '-' }
        }
    }

    init {
        on<ClientChatReceivedEvent>().filter { type.toInt() != 2 && EssentialAPI.getMinecraftUtil().isHypixel() }
            .subscribe {
                API_PATTERN.matchesMatcher(pureText) {
                    apiKey = it.group("api")
                    sendPrefixMessage("§aGrabbed Hypixel API!")
                }
            }
    }
}