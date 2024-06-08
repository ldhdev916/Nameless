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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.overlayParameter
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.gui.feature.components.MultiSelectorComponent
import com.happyandjust.nameless.gui.feature.components.VerticalPositionEditableComponent
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.utils.StatAPIUtils
import com.happyandjust.nameless.utils.Utils
import gg.essential.api.EssentialAPI
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import kotlin.math.max

object InGameStatViewer : SimpleFeature(
    "inGameStatViewer",
    "In Game Stat Viewer",
    "View someone's stat in game. §eRequires hypixel api key"
) {

    init {
        overlayParameter(DisplayType.OVERLAY) {
            matchKeyCategory()
            key = "displayType"
            title = "Display Type"
            desc = DisplayType.entries.joinToString("\n") { "${it.name}: ${it.lore}" }

            config("inGameStatViewer", "overlay", Overlay.DEFAULT)
            component {
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

                        textScale = basicTextScaleConstraint { currentScale.toFloat() }.fixed()
                    } childOf container
                }

                container
            }

            shouldDisplay { enabled && value == DisplayType.OVERLAY }
            render {
                if (!enabled || value != DisplayType.OVERLAY) return@render
                for (player in getPlayersForRender()) {
                    matrix {
                        setup(overlayPoint)

                        mc.fontRendererObj.drawString("§e${player.name}", 0, 0, Color.white.rgb, true)

                        var y = mc.fontRendererObj.FONT_HEIGHT

                        for (identifier in order.filter { it.supportGames.any(SupportGame::shouldDisplay) }) {
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

            parameter(0.0) {
                matchKeyCategory()
                key = "yOffset"
                title = "Y Offset"
                desc = "Offset y from player's head (Only for display type 'HEAD')"

                settings {
                    minValueInt = -5
                    maxValueInt = 5
                }
            }

            parameter(1.0) {
                matchKeyCategory()
                key = "scale"
                title = "Text Scale"
                desc = "Select text scale (Only for display type 'HEAD')"

                settings {
                    ordinal = 1
                    minValue = 0.5
                    maxValue = 5.0
                }
            }

            parameter(true) {
                matchKeyCategory()
                key = "onlyLook"
                title = "Only Looking At"
                desc =
                    "Show stats of only a player you're currently looking at instead of everyone (Only for display type 'HEAD')"

                settings {
                    ordinal = 2
                }
            }
        }

        parameter(emptyList<InGameStatIdentifier>()) {
            matchKeyCategory()
            key = "order"
            title = "Stats List"

            settings {
                ordinal = 3
                allIdentifiers = InformationType.entries.map { InGameStatIdentifier(it, listOf(SupportGame.ALL)) }
            }
        }

        parameter(Unit) {
            matchKeyCategory()
            key = "texts"
            title = "Each Stat Texts"
            desc = "{value} is converted to actual value(like level) when rendering and & will be converted to §"

            componentType = null

            settings { ordinal = 3 }

            for (informationType in InformationType.entries) {
                val informationName = informationType.name.lowercase()
                val statName = informationType.statName

                parameter("$statName: {value}") {
                    matchKeyCategory()
                    key = "${informationName}_text"
                    title = statName
                }
            }
        }
    }

    private fun getPlayersForRender(): List<EntityPlayer> {
        return if (displayType == DisplayType.OVERLAY || displayType_onlyLook) {
            mc.objectMouseOver?.takeIf { it.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY }?.let {
                val entity = it.entityHit
                if (entity is EntityPlayer) arrayListOf(entity) else emptyList()
            } ?: emptyList()
        } else {
            Utils.getPlayersInTab() - mc.thePlayer
        }
    }

    init {
        on<RenderWorldLastEvent>().filter { enabled && displayType == DisplayType.HEAD }.subscribe {
            val render = mc.renderViewEntity

            val renderX = render.getRenderPosX(partialTicks)
            val renderY = render.getRenderPosY(partialTicks)
            val renderZ = render.getRenderPosZ(partialTicks)


            for (player in getPlayersForRender()) {
                matrix {
                    translate(
                        player.posX - renderX,
                        player.posY + player.getEyeHeight() + displayType_yOffset - renderY,
                        player.posZ - renderZ
                    )
                    rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
                    rotate(mc.renderManager.playerViewX, 1f, 0f, 0f)

                    val fontHeight = mc.fontRendererObj.FONT_HEIGHT.toFloat()

                    for (identifier in order.filter { it.supportGames.any(SupportGame::shouldDisplay) }.reversed()) {
                        val text = identifier.informationType.getFormatText(player)

                        val fixedScale = displayType_scale / wrapScaleTo1Block(text)

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
        val format = getParameterValue<String>("texts/${name.lowercase()}_text")

        val value = StatAPIUtils.getStatValue(entityPlayer, this)

        return format.replace("{value}", value).replace("&", "§")
    }


    @Serializable
    data class InGameStatIdentifier(
        val informationType: InformationType,
        var supportGames: List<SupportGame>
    ) : Identifier {
        override fun toUIComponent(gui: VerticalPositionEditableComponent): UIComponent {

            val text = UIText(informationType.statName).constrain {
                y = CenterConstraint()

                textScale = 1.3.pixels()
            }

            val values = SupportGame.entries

            val container = UIContainer().constrain {
                width = ChildBasedSizeConstraint()
                height = basicHeightConstraint { max(text.getHeight(), 20f) }
            }

            text childOf container
            val multiSelector = MultiSelectorComponent(
                supportGames.map { it.name },
                values.map { it.name }).constrain {
                x = SiblingConstraint()
                y = 0.pixel()
            } childOf container

            multiSelector.onValueChange {
                it as List<Int>
                supportGames = it.map(values::get)
                gui.saveValue()
            }
            return container
        }

        override fun areEqual(other: Identifier): Boolean {
            return (other === this) || (other is InGameStatIdentifier && other.informationType === informationType)
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
                return StatAPIUtils.networkExpToLevel(runCatching { jsonObject["networkExp"]!!.double }.getOrDefault(0.0))
                    .toString()
            }
        },
        BEDWARS_LEVEL("BedWars Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject["achievements"]!!.jsonObject["bedwars_level"]!!.int }.getOrDefault(1)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_LEVEL("SkyWars Level") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return StatAPIUtils.skyWarsExpToLevel(
                    runCatching { jsonObject["stats"]!!.jsonObject["SkyWars"]!!.jsonObject["skywars_experience"]!!.int }
                        .getOrDefault(0)
                ).toString()
            }
        },
        ACHIEVEMENT_POINT("Achievement Point") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject["achievementPoints"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        KARMA("Karma") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject["karma"]!!.int }.getOrDefault(0).insertCommaEvery3Character()
            }
        },
        SKYWARS_KILLS("SkyWars Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["kills"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_DEATHS("SkyWars Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["deaths"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_WINS("SkyWars Wins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["wins"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_LOSSES("SkyWars Losses") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["losses"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        SKYWARS_KD("SkyWars K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val kill = runCatching { jsonObject.getGameStat("SkyWars")["kills"]!!.int }.getOrDefault(0)
                val death = runCatching { jsonObject.getGameStat("SkyWars")["deaths"]!!.int }.getOrDefault(0)

                return (kill.toDouble() / death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        SKYWARS_WL("SkyWars W/L") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val win = runCatching { jsonObject.getGameStat("SkyWars")["wins"]!!.int }.getOrDefault(0)
                val loss = runCatching { jsonObject.getGameStat("SkyWars")["deaths"]!!.int }.getOrDefault(0)

                return (win.toDouble() / loss.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        SKYWARS_COINS("SkyWars Coins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("SkyWars")["coins"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_COINS("BedWars Coins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["coins"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_WINSTREAK("BedWars Winstreak") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["winstreak"]!!.int }.getOrDefault(0).toString()
            }
        },
        BEDWARS_KILLS("BedWars Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["kills_bedwars"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_DEATHS("BedWars Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["deaths_bedwars"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_FINAL_KILLS("BedWars Final Kills") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["final_kills_bedwars"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_FINAL_DEATHS("BedWars Final Deaths") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["final_deaths_bedwars"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_WINS("BedWars Wins") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["wins_bedwars"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_LOSSES("BedWars Losses") {
            override fun getStatValue(jsonObject: JsonObject): String {
                return runCatching { jsonObject.getGameStat("Bedwars")["losses_bedwars"]!!.int }.getOrDefault(0)
                    .insertCommaEvery3Character()
            }
        },
        BEDWARS_KD("BedWars K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val kill = runCatching { jsonObject.getGameStat("Bedwars")["kills_bedwars"]!!.int }.getOrDefault(0)
                val death = runCatching { jsonObject.getGameStat("Bedwars")["deaths_bedwars"]!!.int }.getOrDefault(0)

                return (kill.toDouble() / death.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        BEDWARS_FINAL_KD("BedWars Final K/D") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val finalKill =
                    runCatching { jsonObject.getGameStat("Bedwars")["final_kills_bedwars"]!!.int }.getOrDefault(0)
                val finalDeath =
                    runCatching { jsonObject.getGameStat("Bedwars")["final_deaths_bedwars"]!!.int }.getOrDefault(0)

                return (finalKill.toDouble() / finalDeath.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        },
        BEDWARS_WL("BedWars W/L") {
            override fun getStatValue(jsonObject: JsonObject): String {
                val win = runCatching { jsonObject.getGameStat("Bedwars")["wins_bedwars"]!!.int }.getOrDefault(0)
                val loss = runCatching { jsonObject.getGameStat("Bedwars")["losses_bedwars"]!!.int }.getOrDefault(0)

                return (win.toDouble() / loss.coerceAtLeast(1)).transformToPrecisionString(2)
            }
        };


        abstract fun getStatValue(jsonObject: JsonObject): String

        companion object {
            private fun JsonObject.getGameStat(gameName: String) = this["stats"]!!.jsonObject[gameName]!!.jsonObject
        }
    }

    enum class DisplayType(val lore: String) {
        HEAD("On someone's head"), OVERLAY("Render overlay on your screen")
    }
}