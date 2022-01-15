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
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.SubParameterOf
import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.gui.feature.components.VerticalPositionEditableComponent
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.*
import com.happyandjust.nameless.utils.StatAPIUtils
import com.happyandjust.nameless.utils.Utils
import gg.essential.api.EssentialAPI
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
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object InGameStatViewer : SimpleFeature(
    Category.QOL,
    "ingamestatviewer",
    "In Game Stat Viewer",
    "View someone's stat in game. §eRequires hypixel api key"
) {

    private var type by object : OverlayParameter<DisplayType>(
        0,
        "ingamestatviewer",
        "displaytype",
        "Display Type",
        DisplayType.values().joinToString("\n") { "${it.name}: ${it.lore}" },
        DisplayType.OVERLAY,
        getEnumConverter()
    ) {
        override var overlayPoint by ConfigValue("ingamestatviewer", "overlay", Overlay.DEFAULT, COverlay)

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

                    textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()
                } childOf container
            }

            return container
        }

        override fun shouldDisplayInRelocateGui(): Boolean {
            return enabled && value == DisplayType.OVERLAY
        }

        override fun renderOverlay0(partialTicks: Float) {
            if (!enabled || value != DisplayType.OVERLAY) return
            for (player in getPlayersForRender()) {
                matrix {
                    setup(overlayPoint)

                    mc.fontRendererObj.drawString("§e${player.name}", 0, 0, Color.white.rgb, true)

                    var y = mc.fontRendererObj.FONT_HEIGHT

                    for (identifier in order.filter { it.supportGame.shouldDisplay() }) {
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

    @SubParameterOf("type")
    private var yOffset by FeatureParameter(
        0,
        "ingamestatviewer",
        "yoffset",
        "Y Offset",
        "Offset y from player's head (Only for display type 'HEAD')",
        0.0,
        CDouble
    ).apply {
        minValue = -5.0
        maxValue = 5.0
    }

    @SubParameterOf("type")
    private var scale by FeatureParameter(
        1,
        "ingamestatviewer",
        "scale",
        "Text Scale",
        "Select text scale (Only for display type 'HEAD')",
        1.0,
        CDouble
    ).apply {
        minValue = 0.5
        maxValue = 5.0
    }

    @SubParameterOf("type")
    private var onlyLook by FeatureParameter(
        2,
        "ingamestatviewer",
        "onlylook",
        "Only Looking At",
        "Show stats of only a player you're currently looking at instead of everyone (Only for display type 'HEAD')",
        true,
        CBoolean
    )

    var order by FeatureParameter(
        2,
        "ingamestatviewer",
        "order",
        "Stats List",
        "",
        emptyList(),
        CList(InGameStatIdentifier::serialize, InGameStatIdentifier::deserialize)
    ).apply {
        allIdentifiers = InformationType.values().map { InGameStatIdentifier(it, SupportGame.ALL) }
    }

    private var texts by object : FeatureParameter<String>(
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

    private fun getPlayersForRender(): List<EntityPlayer> {
        return if (type == DisplayType.OVERLAY || onlyLook) {
            mc.objectMouseOver?.takeIf { it.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY }?.let {
                val entity = it.entityHit
                if (entity is EntityPlayer) arrayListOf(entity) else emptyList()
            } ?: emptyList()
        } else {
            Utils.getPlayersInTab() - mc.thePlayer
        }
    }

    init {
        on<RenderWorldLastEvent>().filter { enabled && type == DisplayType.HEAD }.subscribe {
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

                    for (identifier in order.filter { it.supportGame.shouldDisplay() }.reversed()) {
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

            jsonObject.add("game", supportGameConverter.serialize(supportGame))
            jsonObject.add("information", informationConverter.serialize(informationType))

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

            private val informationConverter = getEnumConverter<InformationType>()
            private val supportGameConverter = getEnumConverter<SupportGame>()

            fun deserialize(jsonElement: JsonElement): InGameStatIdentifier {
                jsonElement as JsonObject

                return InGameStatIdentifier(
                    informationConverter.deserialize(jsonElement["information"]),
                    supportGameConverter.deserialize(jsonElement["game"])
                )
            }
        }
    }

    enum class SupportGame {
        ALL {
            override fun shouldDisplay() = EssentialAPI.getMinecraftUtil().isHypixel()
        },
        EXCEPT_LOBBY {
            override fun shouldDisplay() = EssentialAPI.getMinecraftUtil().isHypixel() && !Hypixel.inLobby
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
                return StatAPIUtils.networkExpToLevel(runCatching { jsonObject["networkExp"].asDouble }.getOrDefault(0.0))
                    .toString()
            }
        },
        BEDWARS_LEVEL("BedWars Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject["achievements"].asJsonObject["bedwars_level"].asInt }.getOrDefault(1)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_LEVEL("SkyWars Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return StatAPIUtils.skyWarsExpToLevel(
                    runCatching { jsonObject["stats"].asJsonObject["SkyWars"].asJsonObject["skywars_experience"].asInt }
                        .getOrDefault(0)
                ).toString()
            }
        },
        ACHIEVEMENT_POINT("Achievement Point") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject["achievementPoints"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        KARMA("Karma") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject["karma"].asInt }.getOrDefault(0).insertCommaEvery3Character()
            }
        },
        SKYWARS_KILLS("SkyWars Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["kills"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_DEATHS("SkyWars Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["deaths"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_WINS("SkyWars Wins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["wins"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_LOSSES("SkyWars Losses") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["losses"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_KD("SkyWars K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val kill = runCatching { jsonObject.getGameStat("SkyWars")["kills"].asInt }.getOrDefault(0)
                val death = runCatching { jsonObject.getGameStat("SkyWars")["deaths"].asInt }.getOrDefault(0)

                return (kill.toDouble() / death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        SKYWARS_WL("SkyWars W/L") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val win = runCatching { jsonObject.getGameStat("SkyWars")["wins"].asInt }.getOrDefault(0)
                val loss = runCatching { jsonObject.getGameStat("SkyWars")["deaths"].asInt }.getOrDefault(0)

                return (win.toDouble() / loss.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        SKYWARS_COINS("SkyWars Coins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["coins"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_COINS("BedWars Coins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["coins"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_WINSTREAK("BedWars Winstreak") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["winstreak"].asInt }.getOrDefault(0).toString()
            }
        },
        BEDWARS_KILLS("BedWars Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["kills_bedwars"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_DEATHS("BedWars Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["deaths_bedwars"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_FINAL_KILLS("BedWars Final Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["final_kills_bedwars"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_FINAL_DEATHS("BedWars Final Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["final_deaths_bedwars"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_WINS("BedWars Wins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["wins_bedwars"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_LOSSES("BedWars Losses") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["losses_bedwars"].asInt }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_KD("BedWars K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val kill = runCatching { jsonObject.getGameStat("Bedwars")["kills_bedwars"].asInt }.getOrDefault(0)
                val death = runCatching { jsonObject.getGameStat("Bedwars")["deaths_bedwars"].asInt }.getOrDefault(0)

                return (kill.toDouble() / death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        BEDWARS_FINAL_KD("BedWars Final K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val final_kill =
                    runCatching { jsonObject.getGameStat("Bedwars")["final_kills_bedwars"].asInt }.getOrDefault(0)
                val final_death =
                    runCatching { jsonObject.getGameStat("Bedwars")["final_deaths_bedwars"].asInt }.getOrDefault(0)

                return (final_kill.toDouble() / final_death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        BEDWARS_WL("BedWars W/L") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val win = runCatching { jsonObject.getGameStat("Bedwars")["wins_bedwars"].asInt }.getOrDefault(0)
                val loss = runCatching { jsonObject.getGameStat("Bedwars")["losses_bedwars"].asInt }.getOrDefault(0)

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