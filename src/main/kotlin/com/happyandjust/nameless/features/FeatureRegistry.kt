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
            add(FeatureGlowAllPlayers)
            add(FeatureRemoveNegativeEffects)
            add(FeatureBedwarsESP)
        }

        with("Lobby") {
            add(FeatureHideNPC)
        }

        with("In Game") {
            add(FeatureBedwarsRayTraceBed)
            add(FeatureDisplayBetterArmor)
            add(FeatureIndicateParticles)
            add(FeatureTrajectoryPreview)
            add(FeatureShowPingInTab)
            add(FeatureCancelCertainBlockRendering)
            add(FeatureCharm)
            add(FeatureGiftESP)
            add(FeatureDropperHelper)
            add(FeatureBlockTracker)
        }

        with("Mod") {
            add(FeatureRemoveCertainModID)
        }

        with("Miscellaneous") {
            add(FeatureUpdateChecker)
            add(FeatureHitDelayFix)
            add(FeatureStopLogSpamming)
            add(FeatureChangeNicknameColor)
            add(FeatureTextureOverlay)
            add(FeatureDisguiseNickname)
            add(FeatureChangeLeatherArmorColor)
            add(FeatureClickCopyChat)
            add(FeatureChangeSkyColor)
            add(FeatureChangeWorldTime)
        }

        with("Fishing") {
            add(FeatureHideFishHook)
            add(FeatureChangeFishParticleColor)
        }

        with("Damage") {
            add(FeatureNoHurtCam)
            add(FeatureChangeDamagedEntityColor)
        }

        with("Button") {
            add(FeatureHypixelButton)
            add(FeatureReconnectButton)
        }

        with("Hypixel") {
            add(FeaturePlayTabComplete)
            add(FeatureMurdererFinder)
            add(FeatureGTBHelper)
            add(FeaturePartyGamesHelper)
            add(FeatureAutoAcceptParty)
            add(FeatureHideTipMessage)
            add(FeaturePixelPartyHelper)
            add(FeatureInGameStatViewer)
            add(FeatureAutoRequeue)
            add(FeatureJoinHypixelImmediately)
        }

        with("Quality Of Life") {
            add(FeaturePerspective)
            add(FeatureF5Fix)
        }

        with("SkyBlock") {
            add(FeatureFairySoulWaypoint)
            add(FeatureDamageIndicator)
            add(FeatureGlowDroppedItem)
            add(FeatureLividDaggerBackstep)
            add(FeatureShowWitherShieldCoolTime)
            add(FeatureDisableEndermanTeleportation)
            add(FeatureEquipPetSkin)
            add(FeatureChangeHelmetTexture)
            add(FeatureExperimentationTableHelper)
            add(FeatureHideWitherImpactParticle)
            add(FeatureBazaarHelper)
        }

        with("Dungeons") {
            add(FeatureGlowStarDungeonMobs)
            add(FeatureDungeonsDoorKey)
            add(FeatureGlowBats)
            add(FeatureGlowDungeonsTeammates)
        }

        with("Mining") {
            add(FeatureGemstoneESP)
        }

        with("Slayer") {
            add(FeatureClickOpenSlayer)
            add(FeatureEndermanSlayerHelper)
        }

        with("Settings") {
            add(FeatureGhostBlock)
            add(FeatureHypixelAPIKey)
            add(FeatureOutlineMode)
            add(FeatureRelocateGui)
            add(FeatureDebug)
        }
    }

}