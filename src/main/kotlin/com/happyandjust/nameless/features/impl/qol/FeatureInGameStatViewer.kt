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

package com.happyandjust.nameless.features.impl.qol

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.gui.feature.components.VerticalPositionEditableComponent
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.*
import com.happyandjust.nameless.utils.StatAPIUtils
import com.happyandjust.nameless.utils.Utils
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.vigilance.gui.settings.SelectorComponent
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition
import java.awt.Color

object FeatureInGameStatViewer :
    SimpleFeature(
        Category.QOL,
        "ingamestatviewer",
        "In Game Stat Viewer",
        "View someone's stat in game. §eRequires hypixel api key"
    ), WorldRenderListener, RenderOverlayListener {

    init {

        val lores = DisplayType.values().map { "${it.name}: ${it.lore}" }

        parameters["type"] = object : OverlayParameter<DisplayType>(
            0,
            "ingamestatviewer",
            "displaytype",
            "Display Type",
            lores.joinToString("\n"),
            DisplayType.OVERLAY,
            CDisplayType
        ) {

            init {
                allEnumList = DisplayType.values().toList()

                parameters["y"] = FeatureParameter(
                    0,
                    "ingamestatviewer",
                    "yoffset",
                    "Y Offset",
                    "Offset y from player's head (Only for display type 'HEAD')",
                    0.0,
                    CDouble
                ).also { featureParameter ->
                    featureParameter.minValue = -5.0
                    featureParameter.maxValue = 5.0
                }

                parameters["scale"] = FeatureParameter(
                    1,
                    "ingamestatviewer",
                    "scale",
                    "Text Scale",
                    "Select text scale (Only for display type 'HEAD')",
                    1.0,
                    CDouble
                ).also { featureParameter ->
                    featureParameter.minValue = 0.5
                    featureParameter.maxValue = 5.0
                }

                parameters["onlylook"] = FeatureParameter(
                    2,
                    "ingamestatviewer",
                    "onlylook",
                    "Only Looking At",
                    "Show stats of only a player you're currently looking at instead of everyone (Only for display type 'HEAD')",
                    true,
                    CBoolean
                )
            }

            override val overlayPoint = ConfigValue("ingamestatviewer", "overlay", Overlay.DEFAULT, COverlay)

            override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
                val container = UIContainer().constrain {
                    width = ChildBasedMaxSizeConstraint()
                    height = ChildBasedSizeConstraint()
                }

                for (text in arrayOf(
                    "Hypixel Level: 10",
                    "BedWars Level: 999§e✫",
                    "BedWars FKDR: 1.0",
                    "SkyWars Kills: 9999"
                )) {
                    UIText(text).constrain {
                        y = SiblingConstraint()

                        textScale = relocateComponent.currentScale.pixels()

                        relocateComponent.onScaleChange {
                            textScale = it.pixels()
                        }
                    } childOf container
                }

                return container
            }

            override fun shouldDisplayInRelocateGui(): Boolean {
                return enabled && value == DisplayType.OVERLAY
            }

            override fun renderOverlay0(partialTicks: Float) {
                val identifiers = this@FeatureInGameStatViewer.getParameterValue<List<Identifier>>("order")
                    .map { it as InGameStatIdentifier }
                val overlay = overlayPoint.value

                for (player in getPlayersForRender()) {
                    matrix {
                        translate(overlay.point.x, overlay.point.y, 0)
                        scale(overlay.scale, overlay.scale, 1.0)

                        mc.fontRendererObj.drawString("§e${player.name}", 0, 0, Color.white.rgb, true)

                        var y = mc.fontRendererObj.FONT_HEIGHT

                        for (identifier in identifiers.filter { it.supportGame.shouldDisplay() }) {
                            mc.fontRendererObj.drawString(
                                identifier.informationType.getFormatText(player),
                                0,
                                y,
                                Color.white.rgb,
                                true
                            )
                            y += mc.fontRendererObj.FONT_HEIGHT
                        }
                    }
                }
            }

        }

        val stats = InformationType.values().map { InGameStatIdentifier(it, SupportGame.ALL) }

        parameters["order"] = FeatureParameter(
            2,
            "ingamestatviewer",
            "order",
            "Stats List",
            "",
            emptyList(),
            CIdentifierList { InGameStatIdentifier.deserialize(it) }
        ).also {
            it.allIdentifiers = stats
        }

        parameters["texts"] = object : FeatureParameter<String>(
            3,
            "ingamestatviewer",
            "texts",
            "Each Stat Texts",
            "{value} is converted to actual value(like level) when rendering and & will be converted to §",
            "",
            CString
        ) {
            override fun getComponentType(): ComponentType? = null

            init {
                for (informationType in InformationType.values()) {
                    val informationName = informationType.name.lowercase()
                    val statName = informationType.statName


                    parameters[informationName] = FeatureParameter(
                        0,
                        "ingamestatviewer",
                        "${informationName}_text",
                        statName,
                        "",
                        "$statName: {value}",
                        CString
                    )
                }
            }
        }

    }

    private fun getPlayersForRender(): List<EntityPlayer> {
        val parameter = getParameter<DisplayType>("type")

        val displayType = parameter.value

        return if (displayType == DisplayType.OVERLAY || parameter.getParameterValue("onlylook")) {
            mc.objectMouseOver?.takeIf { it.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY }?.let {
                val entity = it.entityHit
                if (entity is EntityPlayer) arrayListOf(entity) else emptyList()
            } ?: emptyList()
        } else {
            Utils.getPlayersInTab() - mc.thePlayer
        }
    }

    override fun renderOverlay(partialTicks: Float) {
        if (enabled) {
            val parameter = getParameter<DisplayType>("type")
            if (parameter.value == DisplayType.OVERLAY) {
                (parameter as OverlayParameter).renderOverlay(partialTicks)
            }
        }
    }

    override fun renderWorld(partialTicks: Float) {
        if (enabled) {
            val parameter = getParameter<DisplayType>("type")
            if (parameter.value == DisplayType.HEAD) {
                val identifiers = getParameterValue<List<InGameStatIdentifier>>("order")
                val yOffset = parameter.getParameterValue<Double>("y")
                val scale = parameter.getParameterValue<Double>("scale")

                val render = mc.renderViewEntity

                val renderX = render.getRenderPosX(partialTicks)
                val renderY = render.getRenderPosY(partialTicks)
                val renderZ = render.getRenderPosZ(partialTicks)


                for (player in getPlayersForRender()) {
                    matrix {
                        translate(
                            player.posX - renderX,
                            player.posY + player.getEyeHeight() + yOffset - renderY,
                            player.posZ - renderZ
                        )
                        rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
                        rotate(mc.renderManager.playerViewX, 1f, 0f, 0f)

                        val fontHeight = mc.fontRendererObj.FONT_HEIGHT.toFloat()

                        for (identifier in identifiers.filter { it.supportGame.shouldDisplay() }.reversed()) {
                            val text = identifier.informationType.getFormatText(player)

                            val fixedScale = scale / wrapScaleTo1Block(text)

                            matrix {
                                scale(-fixedScale, -fixedScale, -fixedScale)
                                mc.fontRendererObj.drawStringWithShadow(
                                    text,
                                    -(mc.fontRendererObj.getStringWidth(text) / 2f),
                                    -fontHeight,
                                    Color.white.rgb
                                )
                            }

                            translate(0.0, (fontHeight * fixedScale), 0.0)
                        }
                    }
                }
            }
        }
    }

    private fun InformationType.getFormatText(entityPlayer: EntityPlayer): String {
        val format = getParameter<String>("texts").getParameterValue<String>(name.lowercase())

        val value = StatAPIUtils.getStatValue(entityPlayer, this)

        return format.replace("{value}", value).replace("&", "§")
    }


    class InGameStatIdentifier(
        val informationType: InformationType,
        var supportGame: SupportGame
    ) : Identifier {
        override fun toUIComponent(gui: VerticalPositionEditableComponent): UIComponent {

            val text = UIText(informationType.statName).constrain {
                textScale = 2.pixels()
            }

            val container = UIContainer().constrain {
                width = ChildBasedSizeConstraint()
                height = basicHeightConstraint { text.getHeight() }
            }

            text childOf container

            val values = SupportGame.values()

            val selector = SelectorComponent(
                values.indexOf(supportGame),
                values.map { it.name }).constrain {

                x = SiblingConstraint()
                y = 0.pixel()
            } childOf container

            selector.onValueChange {
                supportGame = SupportGame.values()[it as Int]
                gui.saveValue()
            }

            return container
        }

        override fun serialize(): JsonElement {
            val jsonObject = JsonObject()

            jsonObject.add("game", CSupportGame.serialize(supportGame))
            jsonObject.add("information", CInformationType.serialize(informationType))

            return jsonObject
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as InGameStatIdentifier

            if (informationType != other.informationType) return false
            if (supportGame != other.supportGame) return false

            return true
        }

        override fun hashCode(): Int {
            var result = informationType.hashCode()
            result = 31 * result + supportGame.hashCode()
            return result
        }

        override fun areEqual(other: Identifier): Boolean {
            return (other === this) || (other is InGameStatIdentifier && other.informationType === informationType)
        }


        companion object {
            fun deserialize(jsonElement: JsonElement): Identifier {
                jsonElement as JsonObject

                return InGameStatIdentifier(
                    CInformationType.deserialize(jsonElement["information"]),
                    CSupportGame.deserialize(jsonElement["game"])
                )
            }
        }
    }

    enum class SupportGame {
        ALL {
            override fun shouldDisplay() = mc.thePlayer.inHypixel()
        },
        EXCEPT_LOBBY {
            override fun shouldDisplay() = mc.thePlayer.inHypixel() && !Hypixel.inLobby
        },
        SKYWARS {
            override fun shouldDisplay() = Hypixel.currentGame == GameType.SKYWARS
        },
        MURDER_MYSTERY {
            override fun shouldDisplay() = Hypixel.currentGame == GameType.MURDER_MYSTERY
        },
        BEDWARS {
            override fun shouldDisplay() = Hypixel.currentGame == GameType.BEDWARS
        };

        abstract fun shouldDisplay(): Boolean
    }

    enum class InformationType(val statName: String) {
        HYPIXEL_LEVEL("Hypixel Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return StatAPIUtils.networkExpToLevel(nullCatch(0.0) { jsonObject["networkExp"].asDouble }).toString()
            }
        },
        BEDWARS_LEVEL("BedWars Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(1) { jsonObject["achievements"].asJsonObject["bedwars_level"].asInt }.insertCommaEvery3Character()
            }
        },
        SKYWARS_LEVEL("SkyWars Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return StatAPIUtils.skyWarsExpToLevel(nullCatch(0) { jsonObject["stats"].asJsonObject["SkyWars"].asJsonObject["skywars_experience"].asInt })
                    .toString()
            }
        },
        ACHIEVEMENT_POINT("Achievement Point") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject["achievementPoints"].asInt }.insertCommaEvery3Character()
            }
        },
        KARMA("Karma") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject["karma"].asInt }.insertCommaEvery3Character()
            }
        },
        SKYWARS_KILLS("SkyWars Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("SkyWars")["kills"].asInt }.insertCommaEvery3Character()
            }
        },
        SKYWARS_DEATHS("SkyWars Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("SkyWars")["deaths"].asInt }.insertCommaEvery3Character()
            }
        },
        SKYWARS_WINS("SkyWars Wins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("SkyWars")["wins"].asInt }.insertCommaEvery3Character()
            }
        },
        SKYWARS_LOSSES("SkyWars Losses") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("SkyWars")["losses"].asInt }.insertCommaEvery3Character()
            }
        },
        SKYWARS_KD("SkyWars K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val kill = nullCatch(0) { jsonObject.getGameStat("SkyWars")["kills"].asInt }
                val death = nullCatch(0) { jsonObject.getGameStat("SkyWars")["deaths"].asInt }

                return (kill.toDouble() / death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        SKYWARS_WL("SkyWars W/L") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val win = nullCatch(0) { jsonObject.getGameStat("SkyWars")["wins"].asInt }
                val loss = nullCatch(0) { jsonObject.getGameStat("SkyWars")["deaths"].asInt }

                return (win.toDouble() / loss.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        SKYWARS_COINS("SkyWars Coins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("SkyWars")["coins"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_COINS("BedWars Coins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["coins"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_WINSTREAK("BedWars Winstreak") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["winstreak"].asInt }.toString()
            }
        },
        BEDWARS_KILLS("BedWars Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["kills_bedwars"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_DEATHS("BedWars Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["deaths_bedwars"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_FINAL_KILLS("BedWars Final Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["final_kills_bedwars"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_FINAL_DEATHS("BedWars Final Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["final_deaths_bedwars"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_WINS("BedWars Wins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["wins_bedwars"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_LOSSES("BedWars Losses") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return nullCatch(0) { jsonObject.getGameStat("Bedwars")["losses_bedwars"].asInt }.insertCommaEvery3Character()
            }
        },
        BEDWARS_KD("BedWars K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val kill = nullCatch(0) { jsonObject.getGameStat("Bedwars")["kills_bedwars"].asInt }
                val death = nullCatch(0) { jsonObject.getGameStat("Bedwars")["deaths_bedwars"].asInt }

                return (kill.toDouble() / death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        BEDWARS_FINAL_KD("BedWars Final K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val final_kill = nullCatch(0) { jsonObject.getGameStat("Bedwars")["final_kills_bedwars"].asInt }
                val final_death = nullCatch(0) { jsonObject.getGameStat("Bedwars")["final_deaths_bedwars"].asInt }

                return (final_kill.toDouble() / final_death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        BEDWARS_WL("BedWars W/L") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val win = nullCatch(0) { jsonObject.getGameStat("Bedwars")["wins_bedwars"].asInt }
                val loss = nullCatch(0) { jsonObject.getGameStat("Bedwars")["losses_bedwars"].asInt }

                return (win.toDouble() / loss.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        };


        abstract fun getStatValue(jsonObject: JsonObject): String

        companion object {
            private fun JsonObject.getGameStat(gameName: String) = this["stats"].asJsonObject[gameName].asJsonObject
        }
    }

    enum class DisplayType(val lore: String) {
        HEAD("On someone's head"), OVERLAY("Render overlay on your screen")
    }
}