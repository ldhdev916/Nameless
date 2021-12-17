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
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.PartyGameChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.processor.Processor
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
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.utils.withAlpha
import net.minecraftforge.common.MinecraftForge
import java.awt.Color

object FeaturePartyGamesHelper : SimpleFeature(Category.QOL, "partygameshelper", "Party Games Helper", "") {

    private var jigsaw by FeatureParameter(
        0,
        "partygames",
        "jigsaw",
        "Jigsaw Rush",
        "Render which key to press on your canvas",
        true,
        CBoolean
    )

    private var rpg16 by FeatureParameter(
        0,
        "partygames",
        "rpg16",
        "RPG 16",
        "Glow player who is at 1 hearts",
        true,
        CBoolean
    )

    @SubParameterOf("rpg16")
    var rpg16Color by FeatureParameter(
        0,
        "partygames",
        "rpg16color",
        "Player Color",
        "",
        Color.red.toChromaColor(),
        CChromaColor
    )

    private var avalanche by FeatureParameter(
        0,
        "partygames",
        "avalanche",
        "Avalanche",
        "Render Box under the slabs",
        true,
        CBoolean
    )

    @SubParameterOf("avalanche")
    var avalancheColor by FeatureParameter(
        0,
        "partygames",
        "avalanchecolor",
        "Box Color",
        "",
        Color.magenta.toChromaColor(),
        CChromaColor
    )

    private var animal by FeatureParameter(
        0,
        "partygames",
        "animal",
        "Animal Slaughter",
        "Glow -50% entity so you don't hit them",
        true,
        CBoolean
    )

    @SubParameterOf("animal")
    var animalColor by FeatureParameter(
        0,
        "partygames",
        "animalcolor",
        "-50% Entity Color",
        "",
        Color.red.toChromaColor(),
        CChromaColor
    )

    private var anvil by FeatureParameter(
        0,
        "partygames",
        "anvil",
        "Anvil Spleef",
        "Render box on where the anvils will land on",
        true,
        CBoolean
    )

    @SubParameterOf("animal")
    var anvilColor by FeatureParameter(
        0,
        "partygames",
        "anvilcolor",
        "Box Color",
        "",
        Color.red.withAlpha(0.4f).toChromaColor(),
        CChromaColor
    )

    private var maze by FeatureParameter(
        0,
        "partygames",
        "maze",
        "Spider Maze",
        "Nothing to explain, maze solver",
        true,
        CBoolean
    )

    private var dive by FeatureParameter(
        0,
        "partygames",
        "dive",
        "Dive",
        "Render 'Exact Box' on where you'll land on, color will be red if you'll collide with block when you land",
        true,
        CBoolean
    )

    @SubParameterOf("dive")
    var diveColor by FeatureParameter(
        0,
        "partygames",
        "divecolor",
        "Box Color",
        "",
        Color.blue.toChromaColor(),
        CChromaColor
    )

    var labEscape by object : OverlayParameter<Boolean>(
        0,
        "partygames",
        "labescape",
        "Lab Escape",
        "Render what key you have to press on your screen",
        true,
        CBoolean
    ) {
        override var overlayPoint by ConfigValue("partygames", "labescapeoverlay", Overlay.DEFAULT, COverlay)

        init {
            LabEscapeProcessor.overlay = { overlayPoint }
        }

        override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
            val container = UIContainer().constrain {
                width = ChildBasedMaxSizeConstraint()
                height = ChildBasedSizeConstraint()
            }

            for (text in arrayOf("1", "1", "2", "3", "2")) {
                UIText(text).constrain {
                    y = SiblingConstraint()

                    textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()

                    color = Color.red.constraint
                } childOf container
            }

            return container
        }

        override fun shouldDisplayInRelocateGui(): Boolean {
            return enabled && Hypixel.currentGame == GameType.PARTY_GAMES
        }

        override fun renderOverlay0(partialTicks: Float) {
            // see LabEscapeProcessor
        }
    }

    private var workshop by FeatureParameter(
        0,
        "partygames",
        "workshop",
        "Workshop",
        "Render what blocks you have to break, and etc",
        true,
        CBoolean
    )

    private var highGround by FeatureParameter(
        0,
        "partygames",
        "highground",
        "High Ground",
        "Glow players whose score is higher than you(only if player is in scoreboard)",
        true,
        CBoolean
    )

    @SubParameterOf("highGround")
    var highGroundColor by FeatureParameter(
        0,
        "partygames",
        "highground_color",
        "Glowing Color",
        "",
        Color.red.toChromaColor(),
        CChromaColor
    )
    private var partyGameType: PartyGamesType? = null

    init {
        on<SpecialTickEvent>().subscribe {

            val type: PartyGamesType? =
                if (Hypixel.currentGame == GameType.PARTY_GAMES) Hypixel.getProperty(PropertyKey.PARTY_GAME_TYPE) else null

            if (type != partyGameType) {
                MinecraftForge.EVENT_BUS.post(PartyGameChangeEvent(partyGameType, type))
            }

            partyGameType = type
        }
    }

    fun getFilter(processor: Processor) = when (processor) {
        AnimalSlaughterProcessor -> {
            { partyGameType == PartyGamesType.ANIMAL_SLAUGHTER && animal }
        }
        AnvilSpleefProcessor -> {
            { partyGameType == PartyGamesType.ANVIL_SPLEEF && anvil }
        }
        AvalancheProcessor -> {
            { partyGameType == PartyGamesType.AVALANCHE && avalanche }
        }
        DiveProcessor -> {
            { partyGameType == PartyGamesType.DIVE && dive }
        }
        JigsawRushProcessor -> {
            { partyGameType == PartyGamesType.JIGSAW_RUSH && jigsaw }
        }
        RPG16Processor -> {
            { partyGameType == PartyGamesType.RPG_16 && rpg16 }
        }
        SpiderMazeProcessor -> {
            { partyGameType == PartyGamesType.SPIDER_MAZE && maze }
        }
        LabEscapeProcessor -> {
            { partyGameType == PartyGamesType.LAB_ESCAPE && labEscape }
        }
        WorkshopProcessor -> {
            { partyGameType == PartyGamesType.WORKSHOP && workshop }
        }
        HighGroundProcessor -> {
            { partyGameType == PartyGamesType.HIGH_GROUND && highGround }
        }
        else -> error("")
    }
}