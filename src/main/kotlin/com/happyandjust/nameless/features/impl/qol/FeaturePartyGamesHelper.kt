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

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.events.PartyGameChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.processor.partygames.*
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels
import net.minecraftforge.common.MinecraftForge
import java.awt.Color

object FeaturePartyGamesHelper : SimpleFeature(Category.QOL, "partygameshelper", "Party Games Helper", ""),
    ClientTickListener {

    init {
        parameters["jigsaw"] =
            FeatureParameter(
                0,
                "partygames",
                "jigsaw",
                "Jigsaw Rush",
                "Render which key to press on your canvas",
                true,
                CBoolean
            )
        parameters["rpg16"] = FeatureParameter(
            0,
            "partygames",
            "rpg16",
            "RPG 16",
            "Glow player who is at 1 hearts",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "partygames",
                "rpg16color",
                "Player Color",
                "",
                Color.red.toChromaColor(),
                CChromaColor
            )
        }
        parameters["avalanche"] = FeatureParameter(
            0,
            "partygames",
            "avalanche",
            "Avalanche",
            "Render Box under the slabs",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "partygames",
                "avalanchecolor",
                "Box Color",
                "",
                Color.magenta.toChromaColor(),
                CChromaColor
            )
        }
        parameters["animal"] = FeatureParameter(
            0,
            "partygames",
            "animal",
            "Animal Slaughter",
            "Glow -50% entity so you don't hit them",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "partygames",
                "animalcolor",
                "-50% Entity Color",
                "",
                Color.red.toChromaColor(),
                CChromaColor
            )
        }
        parameters["anvil"] = FeatureParameter(
            0,
            "partygames",
            "anvil",
            "Anvil Spleef",
            "Render box on where the anvils will land on",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "partygames",
                "anvilcolor",
                "Box Color",
                "",
                Color.red.toChromaColor(),
                CChromaColor
            )
        }
        parameters["maze"] = FeatureParameter(
            0,
            "partygames",
            "maze",
            "Spider Maze",
            "Nothing to explain, maze solver",
            true,
            CBoolean
        )
        parameters["dive"] = FeatureParameter(
            0,
            "partygames",
            "dive",
            "Dive",
            "Render 'Exact Box' on where you'll land on, color will be red if you'll collide with block when you land",
            true,
            CBoolean
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "partygames",
                "divecolor",
                "Box Color",
                "",
                Color.blue.toChromaColor(),
                CChromaColor
            )
        }
        parameters["labescape"] = object : OverlayParameter<Boolean>(
            0,
            "partygames",
            "labescape",
            "Lab Escape",
            "Render what key you have to press on your screen",
            true,
            CBoolean
        ) {
            override val overlayPoint = ConfigValue("partygames", "labescapeoverlay", Overlay.DEFAULT, COverlay)

            override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
                val container = UIContainer().constrain {
                    width = ChildBasedMaxSizeConstraint()
                    height = ChildBasedSizeConstraint()
                }

                for (text in arrayOf("1", "1", "2", "3", "2")) {
                    UIText(text).constrain {
                        y = SiblingConstraint()

                        textScale = relocateComponent.currentScale.pixels()

                        relocateComponent.onScaleChange {
                            textScale = it.pixels()
                        }

                        color = Color.red.constraint
                    } childOf container
                }

                return container
            }

            override fun renderOverlay(partialTicks: Float) {
                // see LabEscapeProcessor
            }

        }

        processors[AnimalSlaughterProcessor.also {
            it.entityColor = { getParameter<Boolean>("animal").getParameterValue<Color>("color").rgb }
        }] =
            { partyGameType == PartyGamesType.ANIMAL_SLAUGHTER && getParameterValue("animal") }

        processors[AnvilSpleefProcessor.also {
            it.boxColor = { getParameter<Boolean>("anvil").getParameterValue<Color>("color").rgb }
        }] =
            { partyGameType == PartyGamesType.ANVIL_SPLEEF && getParameterValue("anvil") }

        processors[AvalancheProcessor.also {
            it.boxColor = { getParameter<Boolean>("avalanche").getParameterValue<Color>("color").rgb }
        }] = { partyGameType == PartyGamesType.AVALANCHE && getParameterValue("avalanche") }

        processors[DiveProcessor.also {
            it.boxColor = { getParameter<Boolean>("dive").getParameterValue<Color>("color").rgb }
        }] = { partyGameType == PartyGamesType.DIVE && getParameterValue("dive") }

        processors[JigsawRushProcessor] = { partyGameType == PartyGamesType.JIGSAW_RUSH && getParameterValue("jigsaw") }

        processors[RPG16Processor.also {
            it.playerColor = { getParameter<Boolean>("rpg16").getParameterValue<Color>("color").rgb }
        }] = { partyGameType == PartyGamesType.RPG_16 && getParameterValue("rpg16") }

        processors[SpiderMazeProcessor] = { partyGameType == PartyGamesType.SPIDER_MAZE && getParameterValue("maze") }
        processors[LabEscapeProcessor.also {
            it.overlay = { (getParameter<Boolean>("labescape") as OverlayParameter).overlayPoint.value }
        }] = { partyGameType == PartyGamesType.LAB_ESCAPE && getParameterValue("labescape") }
    }

    private var partyGameType: PartyGamesType? = null

    override fun tick() {

        val type: PartyGamesType? =
            if (enabled && Hypixel.currentGame == GameType.PARTY_GAMES) Hypixel.getProperty(PropertyKey.PARTY_GAME_TYPE) else null

        if (type != partyGameType) {
            MinecraftForge.EVENT_BUS.post(PartyGameChangeEvent(partyGameType, type))
        }

        partyGameType = type
    }


}