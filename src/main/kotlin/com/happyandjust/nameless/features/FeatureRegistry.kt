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

import com.happyandjust.nameless.MOD_ID
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.features.impl.general.FeatureBedwarsESP
import com.happyandjust.nameless.features.impl.general.FeatureBedwarsRayTraceBed
import com.happyandjust.nameless.features.impl.general.FeatureDisplayBetterArmor
import com.happyandjust.nameless.features.impl.general.FeatureGlowAllPlayers
import com.happyandjust.nameless.features.impl.misc.FeatureClickCopyChat
import com.happyandjust.nameless.features.impl.misc.FeatureDisguiseNickname
import com.happyandjust.nameless.features.impl.misc.FeatureTextureOverlay
import com.happyandjust.nameless.features.impl.misc.FeatureUpdateChecker
import com.happyandjust.nameless.features.impl.qol.*
import com.happyandjust.nameless.features.impl.settings.FeatureGhostBlock
import com.happyandjust.nameless.features.impl.settings.FeatureHypixelAPIKey
import com.happyandjust.nameless.features.impl.settings.FeatureOutlineMode
import com.happyandjust.nameless.features.impl.settings.FeatureRelocateGui
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
    private val featuresByKey = hashMapOf<String, SimpleFeature>()

    private fun <T : SimpleFeature> T.register(inCategory: String = "") = apply {
        features.add(this)

        this.inCategory = inCategory

        val list = featuresByCategory[category] ?: arrayListOf()
        list.add(this)
        featuresByCategory[category] = list

        if (featuresByKey.put(key, this) != null) {
            throw RuntimeException("Duplicate Feature Key")
        }
    }

    fun <T : SimpleFeature> getFeatureByKey(key: String): T {
        return featuresByKey[key] as T
    }

    //GENERAL
    val GLOW_ALL_PLAYERS = FeatureGlowAllPlayers.register("Visual")
    val REMOVE_NEGATIVE_EFFECTS = SimpleFeature(
        Category.GENERAL,
        "removenegativeeffects",
        "Remove Negative Effects",
        "Support Blindness, Nausea"
    ).also {
        it.parameters["blindness"] = FeatureParameter(0, "effects", "blindness", "Blindness", "", true, CBoolean)
        it.parameters["nausea"] = FeatureParameter(1, "effects", "nausea", "Nausea", "", true, CBoolean)
    }.register("Visual")
    val BEDWARS_ESP = FeatureBedwarsESP.register("Visual")
    val HIDE_NPC = FeatureHideNPC.register("Lobby")
    val BEDWARS_RAY_TRACE_BED = FeatureBedwarsRayTraceBed.register("In Game")
    val DISPLAY_BETTER_ARMOR = FeatureDisplayBetterArmor.register("In Game")
    val REMOVE_CERTAIN_MOD_ID = SimpleFeature(
        Category.GENERAL,
        "removemodid",
        "Remove Certain Mod ID Sent to Server",
        enabled_ = true
    ).also {
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
                mod.modId == MOD_ID,
                CBoolean
            )
        }
    }.register("Mod")

    //MISC
    val UPDATE_CHECKER = FeatureUpdateChecker.register("Miscellaneous")
    val HIT_DELAY_FIX = SimpleFeature(
        Category.MISCELLANEOUS,
        "hitdelayfix",
        "Hit Delay Fix",
        "",
        true
    ).register("Miscellaneous")
    val STOP_LOG_SPAMMING = SimpleFeature(
        Category.MISCELLANEOUS,
        "stoplogspamming",
        "Stop Log Spamming",
        "Stops error message spamming in logger when you are in hypixel",
        true
    ).register("Miscellaneous")
    val CHANGE_NICKNAME_COLOR = SimpleFeature(
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
    }.register("Miscellaneous")
    val TEXTURE_OVERLAY = FeatureTextureOverlay.register("Miscellaneous")
    val HIDE_FISH_HOOK = SimpleFeature(
        Category.MISCELLANEOUS,
        "hideothersfishhook",
        "Hide Other Player's Fish Hook"
    ).register("Fishing")
    val CHANGE_FISH_PARTICLE_COLOR =
        SimpleFeature(
            Category.MISCELLANEOUS,
            "changefishparticlecolor",
            "Change Fishing Particle Color",
            ""
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
        }.register("Fishing")
    val DISGUISE_NICKNAME = FeatureDisguiseNickname.register("Miscellaneous")
    val CHANGE_LEATHER_ARMOR_COLOR = SimpleFeature(
        Category.MISCELLANEOUS,
        "changeleatherarmorcolor",
        "Change Leather Armor Color",
        "Customize leather armor color"
    ).also {
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
    }.register("Miscellaneous")
    val NO_HURTCAM = SimpleFeature(
        Category.MISCELLANEOUS,
        "nohurtcam",
        "No HurtCam",
        "Remove the hurt animation when being hit"
    ).register("Damage")
    val CHANGE_DAMAGED_ENTITY_COLOR = SimpleFeature(
        Category.MISCELLANEOUS,
        "changedamagedentitycolor",
        "Change Damaged Entity Color",
        ""
    ).also {
        it.parameters["color"] = FeatureParameter(
            0,
            "damagedentity",
            "color",
            "Damaged Entity Color",
            "",
            Color.red.toChromaColor(),
            CChromaColor
        )
    }.register("Damage")
    val CLICK_COPY_CHAT = FeatureClickCopyChat.register("Miscellaneous")

    //QOL
    val JOIN_HYPIXEL_BUTTON = FeatureHypixelButton.register("Button")
    val RECONNECT_BUTTON = FeatureReconnectButton.register("Button")
    val PLAY_AUTO_TAB_COMPLETE = FeaturePlayTabComplete.register("Hypixel")
    val PERSPECTIVE = FeaturePerspective.register("Quality Of Life")
    val F5_FIX = SimpleFeature(
        Category.QOL,
        "f5fix",
        "F5 Fix",
        "Allow you to look through blocks when using f5",
        true
    ).register("Quality Of Life")
    val MURDERER_FINDER = FeatureMurdererFinder.register("Hypixel")
    val TRAJECTORY_PREVIEW = FeatureTrajectoryPreview.register("In Game")
    val SHOW_PING_NUMBER_IN_TAB = FeatureShowPingInTab.register("In Game")
    val GTB_HELPER = FeatureGTBHelper.register("Hypixel")
    val PARTY_GAMES_HELPER = FeaturePartyGamesHelper.register("Hypixel")
    val AUTO_ACCEPT_PARTY = FeatureAutoAcceptParty.register("Hypixel")
    val AFK_MODE = FeatureAFKMode.register("In Game")
    val HIDE_TIP_MESSAGE = FeatureHideTipMessage.register("Hypixel")
    val CANCEL_CERTAIN_BLOCK_RENDERING = FeatureCancelCertainBlockRendering.register("In Game")
    val PIXEL_PARTY_HELPER = FeaturePixelPartyHelper.register("Hypixel")
    val IN_GAME_STAT_VIEWER = FeatureInGameStatViewer.register("Hypixel")

    //SKYBLOCK
    val FAIRY_SOUL_WAYPOINT = FeatureFairySoulWaypoint.register("SkyBlock")
    val GLOW_STAR_DUNGEON_MOBS = FeatureGlowStarDungeonMobs.register("Dungeons")
    val DAMAGE_INDICATOR = SimpleFeature(
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
            "K, M, B, SMART",
            DamageIndicateType.SMART,
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
    }.register("SkyBlock")
    val DUNGEON_DOOR_KEY = FeatureDungeonsDoorKey.register("Dungeons")
    val JERRY_GIFT_ESP = FeatureJerryGiftESP.register("SkyBlock")
    val GEMSTONE_ESP = FeatureGemstoneESP.register("Mining")
    val GLOW_DROPPED_ITEM = FeatureGlowDroppedItem.register("SkyBlock")
    val LIVID_DAGGER_BACKSTEP_NOTIFIER = FeatureLividDaggerBackstep.register("SkyBlock")
    val GLOW_BATS = FeatureGlowBats.register("Dungeons")
    val GLOW_DUNGEONS_TEAMMATES = FeatureGlowDungeonsTeammates.register("Dungeons")
    val SHOW_WITHER_SHIELD_COOLTIME = FeatureShowWitherShieldCoolTime.register("SkyBlock")
    val CLICK_OPEN_SLAYER = FeatureClickOpenSlayer.register("Slayer")
    val ENDERMAN_SLAYER_HELPER = FeatureEndermanSlayerHelper.register("Slayer")
    val DISABLE_ENDERMAN_TELEPORTATION = FeatureDisableEndermanTeleportation.register("SkyBlock")
    val EQUIP_PET_SKIN = FeatureEquipPetSkin.register("SkyBlock")
    val CHANGE_HELMET_TEXTURE = FeatureChangeHelmetTexture.register("SkyBlock")
    val EXPERIMENTAL_TABLE_HELPER = FeatureExperimentationTableHelper.register("SkyBlock")

    //SETTINGS
    val GHOST_BLOCK = FeatureGhostBlock.register("Settings")
    val HYPIXEL_API_KEY = FeatureHypixelAPIKey.register("Settings")
    val OUTLINE_MODE = FeatureOutlineMode.register("Settings")
    val RELOCATE_GUI = FeatureRelocateGui.register("Settings")


}
