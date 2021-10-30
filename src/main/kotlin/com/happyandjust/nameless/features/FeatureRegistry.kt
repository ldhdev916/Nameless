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

import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.setInCategory
import com.happyandjust.nameless.features.impl.general.FeatureBedwarsESP
import com.happyandjust.nameless.features.impl.general.FeatureBedwarsRayTraceBed
import com.happyandjust.nameless.features.impl.general.FeatureDisplayBetterArmor
import com.happyandjust.nameless.features.impl.general.FeatureGlowAllPlayers
import com.happyandjust.nameless.features.impl.misc.FeatureDisguiseNickname
import com.happyandjust.nameless.features.impl.misc.FeatureTextureOverlay
import com.happyandjust.nameless.features.impl.misc.FeatureUpdateChecker
import com.happyandjust.nameless.features.impl.qol.*
import com.happyandjust.nameless.features.impl.settings.FeatureGhostBlock
import com.happyandjust.nameless.features.impl.settings.FeatureReparty
import com.happyandjust.nameless.features.impl.skyblock.*
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.serialization.converters.CDamageIndicateType
import com.happyandjust.nameless.serialization.converters.CInt
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.ModContainer
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
    val GLOW_ALL_PLAYERS = register(FeatureGlowAllPlayers.setInCategory("Use At Your Own Risk"))
    val REMOVE_NEGATIVE_EFFECTS = register(
        SimpleFeature(
            Category.GENERAL,
            "removenegativeeffects",
            "Remove Negative Effects",
            "Support Blindness, Nausea"
        ).also {
            it.parameters["blindness"] = FeatureParameter(0, "effects", "blindness", "Blindness", "", true, CBoolean)
            it.parameters["nausea"] = FeatureParameter(1, "effects", "nausea", "Nausea", "", true, CBoolean)
        }.setInCategory("Use At Your Own Risk")
    )
    val BEDWARS_ESP = register(FeatureBedwarsESP.setInCategory("Use At Your Own Risk"))
    val HIDE_NPC = register(FeatureHideNPC.setInCategory("Lobby"))
    val BEDWARS_RAY_TRACE_BED = register(FeatureBedwarsRayTraceBed.setInCategory("In Game"))
    val DISPLAY_BETTER_ARMOR = register(FeatureDisplayBetterArmor.setInCategory("In Game"))
    val REMOVE_CERTAIN_MOD_ID =
        register(
            SimpleFeature(
                Category.GENERAL,
                "removemodid",
                "Remove Certain Mod ID Sent to Server"
            ).setInCategory("Mod").also {
                val mods = Loader::class.java.getDeclaredField("mods").also { field ->
                    field.isAccessible = true
                }[Loader.instance()] as List<ModContainer>

                for (mod in mods) {
                    it.parameters[mod.modId] = FeatureParameter(
                        0,
                        "removemodid",
                        mod.modId,
                        "${mod.name} ${mod.version}",
                        "Source File: ${mod.source.absolutePath}",
                        false,
                        CBoolean
                    )
                }
            })

    //MISC
    val UPDATE_CHECKER = register(FeatureUpdateChecker.setInCategory("Miscellaneous"))
    val HIT_DELAY_FIX = register(
        SimpleFeature(
            Category.MISCELLANEOUS,
            "hitdelayfix",
            "Hit Delay Fix",
            "",
            true
        ).setInCategory("Miscellaneous")
    )
    val STOP_LOG_SPAMMING = register(
        SimpleFeature(
            Category.MISCELLANEOUS,
            "stoplogspamming",
            "Stop Log Spamming",
            "Stops error message spamming in logger when you are in hypixel",
            true
        ).setInCategory("Miscellaneous")
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
                CChromaColor
            )
        }.setInCategory("Miscellaneous")
    )
    val TEXTURE_OVERLAY = register(FeatureTextureOverlay.setInCategory("Miscellaneous"))
    val HIDE_FISH_HOOK =
        register(
            SimpleFeature(
                Category.MISCELLANEOUS,
                "hideothersfishhook",
                "Hide Other Player's Fish Hook"
            ).setInCategory("Fishing")
        )
    val CHANGE_FISH_PARTICLE_COLOR =
        register(
            SimpleFeature(
                Category.MISCELLANEOUS,
                "changefishparticlecolor",
                "Change Fishing Particle Color",
                ""
            )
        ).also {
            it.parameters["color"] = FeatureParameter(
                0,
                "fishparticlecolor",
                "color",
                "Fishing Particle Color",
                "",
                Color.red.toChromaColor(),
                CChromaColor
            )
        }.setInCategory("Fishing")
    val DISGUISE_NICKNAME = register(FeatureDisguiseNickname.setInCategory("Miscellaneous"))
    val CHANGE_LEATHER_ARMOR_COLOR = register(
        SimpleFeature(
            Category.MISCELLANEOUS,
            "changeleatherarmorcolor",
            "Change Leather Armor Color",
            "Customize leather armor color"
        ).setInCategory("Miscellaneous").also {
            val parameter: (Int, String) -> Unit = { ordinal, name ->
                val key = name.lowercase()

                it.parameters[key] = FeatureParameter(
                    ordinal,
                    "leatherarmorcolor",
                    key,
                    "Customize $name Color",
                    "",
                    true,
                    CBoolean
                ).also { featureParameter ->
                    featureParameter.parameters["color"] = FeatureParameter(
                        0,
                        "leatherarmorcolor",
                        "${key}_color",
                        "Leather $name Color",
                        "",
                        Color.white.toChromaColor(),
                        CChromaColor
                    )
                }
            }

            for ((ordinal, name) in arrayOf("Helmet", "Chestplate", "Leggings", "Boots").withIndex()) {
                parameter(ordinal, name)
            }
        }
    )

    //QOL
    val JOIN_HYPIXEL_BUTTON = register(FeatureHypixelButton.setInCategory("Button"))
    val RECONNECT_BUTTON = register(FeatureReconnectButton.setInCategory("Button"))
    val PLAY_AUTO_TAB_COMPLETE = register(FeaturePlayTabComplete.setInCategory("Hypixel"))
    val PERSPECTIVE = register(FeaturePerspective.setInCategory("Use At Your Own Risk"))
    val F5_FIX =
        register(
            SimpleFeature(
                Category.QOL,
                "f5fix",
                "F5 Fix",
                "Allow you to look through blocks when using f5",
                true
            ).setInCategory("Use At Your Own Risk")
        )
    val MURDERER_FINDER = register(FeatureMurdererFinder.setInCategory("Hypixel"))
    val TRAJECTORY_PREVIEW = register(FeatureTrajectoryPreview.setInCategory("In Game"))
    val SHOW_PING_NUMBER_IN_TAB = register(FeatureShowPingInTab.setInCategory("In Game"))
    val GTB_HELPER = register(FeatureGTBHelper.setInCategory("Hypixel"))
    val PARTY_GAMES_HELPER = register(FeaturePartyGamesHelper.setInCategory("Hypixel"))
    val AUTO_ACCEPT_PARTY = register(FeatureAutoAcceptParty.setInCategory("Hypixel"))
    val AFK_MODE = register(FeatureAFKMode.setInCategory("In Game"))
    val HIDE_TIP_MESSAGE = register(FeatureHideTipMessage.setInCategory("Hypixel"))
    val CANCEL_CERTAIN_BLOCK_RENDERING = register(FeatureCancelCertainBlockRendering.setInCategory("In Game"))
    val PIXEL_PARTY_HELPER = register(FeaturePixelPartyHelper.setInCategory("Hypixel"))

    //SKYBLOCK
    val FAIRY_SOUL_WAYPOINT = register(FeatureFairySoulWaypoint.setInCategory("SkyBlock"))
    val GLOW_STAR_DUNGEON_MOBS = register(FeatureGlowStarDungeonMobs.setInCategory("Dungeons"))
    val TRACK_AUCTION = register(FeatureTrackAuction.setInCategory("SkyBlock"))
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
                CDamageIndicateType
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
                CInt
            ).also { featureParameter ->
                featureParameter.minValue = 0.0
                featureParameter.maxValue = 7.0
            }
        }.setInCategory("SkyBlock")
    )
    val CHANGE_ITEM_NAME = register(FeatureChangeItemName.setInCategory("SkyBlock"))
    val DUNGEON_DOOR_KEY = register(FeatureDungeonsDoorKey.setInCategory("Dungeons"))
    val BLAZE_SOLVER = register(FeatureBlazeSolver.setInCategory("Dungeons"))
    val JERRY_GIFT_ESP = register(FeatureJerryGiftESP.setInCategory("SkyBlock"))
    val GEMSTONE_ESP = register(FeatureGemstoneESP.setInCategory("Mining"))
    val GLOW_DROPPED_ITEM = register(FeatureGlowDroppedItem.setInCategory("SkyBlock"))
    val LIVID_DAGGER_BACKSTEP_NOTIFIER = register(FeatureLividDaggerBackstep.setInCategory("SkyBlock"))
    val GLOW_BATS = register(FeatureGlowBats.setInCategory("Dungeons"))
    val GLOW_DUNGEONS_TEAMMATES = register(FeatureGlowDungeonsTeammates.setInCategory("Dungeons"))

    //SETTINGS
    val GHOST_BLOCK = register(FeatureGhostBlock.setInCategory("Settings"))
    val REPARTY = register(FeatureReparty.setInCategory("Settings"))


}
