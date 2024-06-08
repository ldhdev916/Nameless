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
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.PartyGameChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.overlayParameter
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.processor.partygames.*
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

object PartyGamesHelper : SimpleFeature("partyGamesHelper", "Party Games Helper", "") {

    init {
        parameter(true) {
            matchKeyCategory()
            key = "jigsaw"
            title = "Jigsaw Rush"
            desc = "Render which key to press on your canvas"
        }

        parameter(true) {
            matchKeyCategory()
            key = "rpg16"
            title = "RPG 16"
            desc = "Glow player who is at 1 heart"

            parameter(Color.red.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Player Color"
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "avalanche"
            title = "Avalanche"
            desc = "Render box under the slabs"

            parameter(Color.magenta.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Box Color"
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "animal"
            title = "Animal Slaughter"
            desc = "Glow -50% entity so you don't hit them"

            parameter(Color.red.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "-50% Entity Color"
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "anvil"
            title = "Anvil Spleef"
            desc = "Render box on where the anvils will land on"

            parameter(Color.red.withAlpha(0.4f).toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Box Color"
            }
        }

        parameter(true) {
            matchKeyCategory()
            key = "maze"
            title = "Spizer Maze"
            desc = "Nothing to explain, maze solver"
        }

        parameter(true) {
            matchKeyCategory()
            key = "dive"
            title = "Dive"
            desc =
                "Render 'Exact Box' on where you'll land on, color will turn into red if you'll collide with block when you land"

            parameter(Color.blue.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Box Color"
            }
        }

        overlayParameter(true) {
            matchKeyCategory()
            key = "labEscape"
            title = "Lab Escape"
            desc = "Render what key you have to press on your screen"

            LabEscapeProcessor.overlay = { overlayPoint }

            config("partyGamesHelper", "labEscapeOverlay", Overlay.DEFAULT)

            component {
                val container = UIContainer().constrain {
                    width = ChildBasedMaxSizeConstraint()
                    height = ChildBasedSizeConstraint()
                }

                for (text in arrayOf("1", "1", "2", "3", "2")) {
                    UIText(text).constrain {
                        y = SiblingConstraint()

                        textScale = basicTextScaleConstraint { currentScale.toFloat() }.fixed()

                        color = Color.red.constraint
                    } childOf container
                }

                container
            }

            shouldDisplay { enabled && value && Hypixel.currentGame == GameType.PARTY_GAMES }

            render { /* LabEscapeProcessor */ }
        }

        parameter(true) {
            matchKeyCategory()
            key = "workshop"
            title = "Workshop"
            desc = "Render what blocks you have to break, and etc"
        }

        parameter(true) {
            matchKeyCategory()
            key = "highGround"
            title = "High Ground"
            desc = "Glow players whose score is higher than you(only if player is in scoreboard)"

            parameter(Color.red.toChromaColor()) {
                matchKeyCategory()
                key = "color"
                title = "Glowing Color"
            }
        }
    }

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

        runCatching {
            getFilter(object : Processor() {
                override val filter = { false }
            }) // weird but this is the way to initialize all processors
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