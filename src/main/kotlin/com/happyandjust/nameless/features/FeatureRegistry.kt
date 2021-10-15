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

package com.happyandjust.nameless.features

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.features.impl.general.*
import com.happyandjust.nameless.features.impl.misc.FeatureTextureOverlay
import com.happyandjust.nameless.features.impl.misc.FeatureUpdateChecker
import com.happyandjust.nameless.features.impl.qol.*
import com.happyandjust.nameless.features.impl.skyblock.*
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import com.happyandjust.nameless.serialization.TypeRegistry
import java.awt.Color

object FeatureRegistry {

    val features = arrayListOf<SimpleFeature>()
    val featuresByCategory = hashMapOf<Category, ArrayList<SimpleFeature>>()
    val featuresByKey = hashMapOf<String, SimpleFeature>()

    private fun <T : SimpleFeature> register(feature: T): T {
        features.add(feature)

        val list = featuresByCategory[feature.category] ?: arrayListOf()

        list.add(feature)

        featuresByCategory[feature.category] = list

        if (featuresByKey.containsKey(feature.key)) {
            throw RuntimeException("Duplicate Feature Key")
        }

        featuresByKey[feature.key] = feature

        return feature
    }

    fun <T : SimpleFeature> getFeatureByKey(key: String): T {
        return featuresByKey[key] as T
    }

    //GENERAL
    val GLOW_ALL_PLAYERS = register(FeatureGlowAllPlayers())
    val GHOST_BLOCK = register(FeatureGhostBlock())
    val REMOVE_NEGATIVE_EFFECTS = register(
        SimpleFeature(
            Category.GENERAL,
            "removenegativeeffects",
            "Remove Negative Effects",
            "Support Blindness, Nausea"
        ).also {
            val cBoolean = TypeRegistry.getConverterByClass(Boolean::class)

            it.parameters["blindness"] = FeatureParameter(0, "effects", "blindness", "Blindness", "", true, cBoolean)
            it.parameters["nausea"] = FeatureParameter(1, "effects", "nausea", "Nausea", "", true, cBoolean)
        }
    )
    val BEDWARS_ESP = register(FeatureBedwarsESP())
    val HIDE_NPC = register(FeatureHideNPC())
    val BEDWARS_RAY_TRACE_BED = register(FeatureBedwarsRayTraceBed())
    val DISPLAY_BETTER_ARMOR = register(FeatureDisplayBetterArmor())

    //MISC
    val UPDATE_CHECKER = register(FeatureUpdateChecker())
    val HIT_DELAY_FIX = register(SimpleFeature(Category.MISCELLANEOUS, "hitdelayfix", "Hit Delay Fix", "", true))
    val STOP_LOG_SPAMMING = register(
        SimpleFeature(
            Category.MISCELLANEOUS,
            "stoplogspamming",
            "Stop Log Spamming",
            "Stops error message spamming in logger when you are in hypixel",
            true
        )
    )
    val CHANGE_NICKNAME_COLOR = register(
        SimpleFeature(
            Category.MISCELLANEOUS,
            "nicknamecolor",
            "Change Nickname Color",
            "Customize your nickname color",
            false
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "nickname",
                "color",
                "Nickname Color",
                "",
                Color.white.toChromaColor(),
                TypeRegistry.getConverterByClass(ChromaColor::class)
            )
        }
    )
    val TEXTURE_OVERLAY = register(FeatureTextureOverlay())

    //QOL
    val JOIN_HYPIXEL_BUTTON = register(FeatureHypixelButton())
    val RECONNECT_BUTTON = register(FeatureReconnectButton())
    val PLAY_AUTO_TAB_COMPLETE = register(FeaturePlayTabComplete())
    val PERSPECTIVE = register(FeaturePerspective())
    val F5_FIX =
        register(SimpleFeature(Category.QOL, "f5fix", "F5 Fix", "Allow you to look through blocks when using f5", true))
    val MURDERER_FINDER = register(FeatureMurdererFinder())
    val TRAJECTORY_PREVIEW = register(FeatureTrajectoryPreview())
    val SHOW_PING_NUMBER_IN_TAB = register(FeatureShowPingInTab())
    val GTB_HELPER = register(FeatureGTBHelper())
    val PARTY_GAMES_HELPER = register(FeaturePartyGamesHelper())
    val AUTO_ACCEPT_PARTY = register(FeatureAutoAcceptParty())
    val AFK_MODE = register(FeatureAFKMode())
    val HIDE_TIP_MESSAGE = register(FeatureHideTipMessage())
    val CANCEL_CERTAIN_BLOCK_RENDERING = register(FeatureCancelCertainBlockRendering())

    //SKYBLOCK
    val FAIRY_SOUL_WAYPOINT = register(FeatureFairySoulWaypoint())
    val GLOW_STAR_DUNGEON_MOBS = register(FeatureGlowStarDungeonMobs())
    val TRACK_AUCTION = register(FeatureTrackAuction())
    val DAMAGE_INDICATOR = register(
        SimpleFeature(
            Category.SKYBLOCK,
            "damageindicator",
            "Damage Indicator",
            "Transform damage into K or M or B"
        ).also {
            it.parameters["type"] = FeatureParameter(
                0,
                "damageindicator",
                "type",
                "Damage Indicate Type",
                "K, M, B",
                DamageIndicateType.M,
                TypeRegistry.getConverterByClass(DamageIndicateType::class)
            ).also { featureParameter ->
                featureParameter.allEnumList = DamageIndicateType.values().toList()
            }

            it.parameters["precision"] = FeatureParameter(
                0,
                "damageindicator",
                "precision",
                "Precision",
                "",
                1,
                TypeRegistry.getConverterByClass(Int::class)
            ).also { featureParameter ->
                featureParameter.minValue = 0.0
                featureParameter.maxValue = 7.0
            }
        }
    )
    val CHANGE_ITEM_NAME = register(FeatureChangeItemName())
    val DUNGEON_DOOR_KEY = register(FeatureDungeonsDoorKey())
    val BLAZE_SOLVER = register(FeatureBlazeSolver())


}
