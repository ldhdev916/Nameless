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
import com.happyandjust.nameless.features.impl.*
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

    //SKYBLOCK
    val FAIRY_SOUL_WAYPOINT = register(FeatureFairySoulWaypoint())
    val GLOW_STAR_DUNGEON_MOBS = register(FeatureGlowStarDungeonMobs())


}
