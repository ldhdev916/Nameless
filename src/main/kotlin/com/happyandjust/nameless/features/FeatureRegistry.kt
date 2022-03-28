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

package com.happyandjust.nameless.features

import com.happyandjust.nameless.features.base.BaseFeature
import com.happyandjust.nameless.features.impl.general.*
import com.happyandjust.nameless.features.impl.misc.*
import com.happyandjust.nameless.features.impl.qol.*
import com.happyandjust.nameless.features.impl.settings.*
import com.happyandjust.nameless.features.impl.skyblock.*
import net.minecraftforge.fml.common.Loader

object FeatureRegistry {

    val features = arrayListOf<BaseFeature<*>>()
    val featuresByCategory
        get() = features.groupBy { it.category }

    private fun String.add(feature: BaseFeature<*>) {
        features.add(feature)

        feature.propertySetting.subCategory = this

        feature.parseParameters()
    }

    private fun <T : BaseFeature<*>> T.parseParameters() {
        if (!categoryInitialized) {
            val fullName = javaClass.name

            category = when (val packageName =
                fullName.substringAfter("com.happyandjust.nameless.features.impl.").substringBefore(".")) {
                "general" -> Category.GENERAL
                "misc" -> Category.MISCELLANEOUS
                "qol" -> Category.QOL
                "settings" -> Category.SETTINGS
                "skyblock" -> Category.SKYBLOCK
                else -> error("Unexpected package name \"$packageName\"")
            }
        }
    }

    init {
        with("Visual") {
            add(GlowAllPlayers)
            add(RemoveNegativeEffects)
            add(BedWarsESP)
        }

        with("Lobby") {
            add(HideNPC)
        }

        with("In Game") {
            add(BedWarsRayTraceBed)
            add(DisplayBetterArmor)
            add(IndicateParticles)
            add(TrajectoryPreview)
            add(ShowPingInTab)
            add(CancelCertainBlockRendering)
            add(Charm)
            add(GiftESP)
            add(DropperHelper)
            //   add(BlockTracker)
        }

        with("Mod") {
            add(RemoveCertainModID)
        }

        with("Miscellaneous") {
            add(UpdateChecker)
            add(HitDelayFix)
            add(StopLogSpamming)
            add(ChangeNicknameColor)
            add(TextureOverlay)
            add(DisguiseNickname)
            add(ChangeLeatherArmorColor)
            add(ClickCopyChat)
            add(ChangeSkyColor)
            add(ChangeWorldTime)
        }

        with("Fishing") {
            add(HideFishHook)
            add(ChangeFishParticleColor)
        }

        with("Damage") {
            add(NoHurtCam)
            add(ChangeDamagedEntityColor)
        }

        with("Button") {
            add(AddHypixelButton)
            add(AddReconnectButton)
        }

        with("Hypixel") {
            add(PlayTabComplete)
            add(MurdererFinder)
            add(GTBHelper)
            add(PartyGamesHelper)
            add(AutoAcceptParty)
            add(HideTipMessage)
            add(PixelPartyHelper)
            add(InGameStatViewer)
            add(AutoRequeue)
            add(JoinHypixelImmediately)
            add(HyChatChannelChanger)
        }

        with("Quality Of Life") {
            add(Perspective)
            add(F5Fix)
        }

        with("SkyBlock") {
            add(FairySoulWaypoint)
            add(DamageIndicator)
            add(GlowDroppedItem)
            add(LividDaggerBackStep)
            add(ShowWitherShieldCoolTime)
            add(DisableEndermanTeleportation)
            add(EquipPetSkin)
            add(ChangeHelmetTexture)
            add(ExperimentationTableHelper)
            add(HideWitherImpactParticle)
            add(BazaarHelper)
        }

        with("Dungeons") {
            add(GlowStarDungeonMobs)
            add(ShowDungeonsDoorKey)
            add(GlowDungeonsBats)
            add(GlowDungeonsTeammates)
        }

        with("Mining") {
            add(GemstoneESP)
        }

        with("Slayer") {
            add(ClickOpenSlayer)
            add(EndermanSlayerHelper)
        }

        with("Settings") {
            add(GhostBlock)
            add(HypixelAPIKey)
            add(OutlineMode)
            add(OpenRelocateGui)
            add(Debug)
            if (Loader.isModLoaded("skyblockaddons")) add(DisableSBAGlowing)
        }
    }

}