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

import com.happyandjust.nameless.features.base.FeatureParameter
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.impl.general.*
import com.happyandjust.nameless.features.impl.misc.*
import com.happyandjust.nameless.features.impl.qol.*
import com.happyandjust.nameless.features.impl.settings.*
import com.happyandjust.nameless.features.impl.skyblock.*
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

object FeatureRegistry {

    val features = arrayListOf<SimpleFeature>()
    val featuresByCategory = hashMapOf<Category, ArrayList<SimpleFeature>>()

    private fun String.add(feature: SimpleFeature) {
        features.add(feature)

        feature.inCategory = this

        featuresByCategory.getOrPut(feature.category) { arrayListOf() }.add(feature)
        feature.parseParameters()
    }

    private fun <T : SimpleFeature> T.parseParameters() {

        val processSubParameters = hashSetOf<() -> Unit>()

        for (property in this::class.memberProperties.filterIsInstance<KMutableProperty1<T, *>>()
            .onEach { it.isAccessible = true }) {
            val delegate = property.getDelegate(this)
            if (delegate is FeatureParameter<*>) {
                property.findAnnotation<InCategory>()?.let {
                    delegate.inCategory = it.inCategory
                }

                val subParameterOf = property.findAnnotation<SubParameterOf>()

                if (subParameterOf != null) {
                    processSubParameters.add {
                        parameters[subParameterOf.parameterProperty]!!.parameters[property.name] = delegate
                    }
                } else {
                    parameters[property.name] = delegate
                }
            }
        }

        processSubParameters.forEach { it() }
    }

    init {
        with("Visual") {
            add(GlowAllPlayers)
            add(RemoveNegativeEffects)
            add(BedwarsESP)
        }

        with("Lobby") {
            add(HideNPC)
        }

        with("In Game") {
            add(BedwarsRayTraceBed)
            add(DisplayBetterArmor)
            add(IndicateParticles)
            add(TrajectoryPreview)
            add(ShowPingInTab)
            add(CancelCertainBlockRendering)
            add(Charm)
            add(GiftESP)
            add(DropperHelper)
            add(BlockTracker)
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
            add(LividDaggerBackstep)
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
            add(DisableSBAGlowing)
        }
    }

}