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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.KeyPressEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.fairysoul.FairySoulProfileCache
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object FeatureFairySoulWaypoint : SimpleFeature(
    Category.SKYBLOCK,
    "fairysoulwaypoint",
    "FairySoul Waypoint",
    "Renders outline box on fairysoul except the ones you've already found"
) {

    private val PROFILE = "You are playing on profile: (?<profile>\\w+).*".toPattern()
    private val PROFILE_CHANGE = "Your profile was changed to: (?<profile>\\w+).*".toPattern()

    var currentSkyblockIsland: String? = null
    private var fairySoulPaths = listOf<BlockPos>()
    private var currentIslandFairySouls = listOf<FairySoul>()
    private val fairySoulColor = Color(0, 0, 255, 134).rgb
    private val freezedPathColor = Color(95, 95, 229).rgb
    private var pathFreezed = false

    private val pathTimer = TickTimer.withSecond(1)
    private val dungeonFairySoulScanTimer = TickTimer.withSecond(1)

    private var foundFairySoulsInThisProfile = emptyList<FairySoul>()
    private var currentSkyBlockProfile = "Unknown"
        set(value) {
            if (field != value) {
                field = value
                FairySoulProfileCache.changeToProfileAndIfNotExistThenCreate(value)
            }
        }

    private var showPath by FeatureParameter(
        0,
        "fairysoulwaypoint",
        "path",
        "Fairy Soul Path Finding",
        "Show path to nearest fairysoul",
        true,
        CBoolean
    )

    init {
        on<RenderWorldLastEvent>().filter { enabled && Hypixel.currentGame == GameType.SKYBLOCK }.subscribe {
            if (showPath) {
                RenderUtils.drawPath(fairySoulPaths, if (pathFreezed) freezedPathColor else Color.red.rgb, partialTicks)
            }
            for (fairySoul in currentIslandFairySouls - foundFairySoulsInThisProfile.toSet()) {
                RenderUtils.drawBox(fairySoul.toBlockPos().getAxisAlignedBB(), fairySoulColor, partialTicks)
            }
        }

        on<SpecialTickEvent>().filter { enabled && Hypixel.currentGame == GameType.SKYBLOCK }.subscribe {
            currentSkyblockIsland?.let {
                if (it == "dungeon" && dungeonFairySoulScanTimer.update().check()) {
                    currentIslandFairySouls = SkyblockUtils.getAllFairySoulsByEntity("dungeon")
                }

                foundFairySoulsInThisProfile =
                    FairySoulProfileCache.currentlyLoadedProfile.foundFairySouls[currentSkyblockIsland] ?: emptyList()

                if (showPath && pathTimer.update().check()) {
                    currentIslandFairySouls
                        .filter { fairySoul -> fairySoul !in foundFairySoulsInThisProfile }
                        .takeIf { list -> list.any() && !pathFreezed }
                        ?.map(FairySoul::toBlockPos)
                        ?.minByOrNull(mc.thePlayer::getDistanceSq)
                        ?.let { blockPos ->
                            fairySoulPaths = ModPathFinding(blockPos, true).findPath()
                        }
                }
            } ?: run {
                currentSkyblockIsland = Hypixel.getProperty(PropertyKey.ISLAND)

                currentSkyblockIsland?.let {
                    currentIslandFairySouls = SkyblockUtils.getAllFairySoulsInWorld(it)
                }

            }
        }

        on<HypixelServerChangeEvent>().subscribe {
            currentSkyblockIsland = null
            currentSkyBlockProfile = "Unknown"
        }

        on<KeyPressEvent>().filter { !inGui && isNew && keyBindingCategory == KeyBindingCategory.FREEZE_FAIRYSOUL_PATHS }
            .subscribe {
                pathFreezed = !pathFreezed
            }

        on<PacketEvent.Sending>().filter { Hypixel.currentGame == GameType.SKYBLOCK }.subscribe {
            packet.withInstance<C02PacketUseEntity> {
                if (action == C02PacketUseEntity.Action.ATTACK) {
                    val entity = getEntityFromWorld(mc.theWorld)
                    if (entity is EntityArmorStand && entity.isFairySoul()) {
                        FairySoulProfileCache.currentlyLoadedProfile.addFoundFairySoul(
                            currentSkyblockIsland ?: return@subscribe,
                            BlockPos(entity.posX, entity.posY + 2, entity.posZ)
                        )
                    }
                }
            }
        }

        on<ClientChatReceivedEvent>().filter { type.toInt() != 2 && Hypixel.currentGame == GameType.SKYBLOCK }
            .subscribe {
                PROFILE.matchesMatcher(pureText) {
                    currentSkyBlockProfile = it.group("profile")
                }
                PROFILE_CHANGE.matchesMatcher(pureText) {
                    currentSkyBlockProfile = it.group("profile")
                }
            }
    }

}