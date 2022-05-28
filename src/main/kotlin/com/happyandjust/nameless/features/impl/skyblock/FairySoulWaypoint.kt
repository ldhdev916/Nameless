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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.KeyPressEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.fairysoul.FairySoulProfileCache
import com.happyandjust.nameless.hypixel.games.SkyBlock
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.utils.SkyblockUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

@OptIn(DelicateCoroutinesApi::class)
object FairySoulWaypoint : SimpleFeature(
    "fairySoulWaypoint",
    "FairySoul Waypoint",
    "Renders outline box on fairysoul except the ones you've already found"
) {

    init {
        hierarchy {
            +::showPath
        }
    }

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
                FairySoulProfileCache.changeToProfile(value)
            }
        }

    private var showPath by parameter(true) {
        key = "showPath"
        title = "FairySoul Path Finding"
        desc = "Show path to nearest fairysoul"
    }


    init {
        on<RenderWorldLastEvent>().filter { enabled && Nameless.hypixel.currentGame is SkyBlock }.subscribe {
            if (showPath) {
                val color = if (pathFreezed) freezedPathColor else Color.red.rgb
                fairySoulPaths.drawPaths(color, partialTicks)
            }
            for (fairySoul in currentIslandFairySouls - foundFairySoulsInThisProfile.toSet()) {
                fairySoul.blockPos.getAxisAlignedBB().drawFilledBox(fairySoulColor, partialTicks)
            }
        }

        on<SpecialTickEvent>().filter { enabled }.subscribe {
            val currentGame = Nameless.hypixel.currentGame
            if (currentGame !is SkyBlock) return@subscribe

            currentSkyblockIsland?.let {
                if (it == "dungeon" && dungeonFairySoulScanTimer.update().check()) {
                    currentIslandFairySouls = SkyblockUtils.getAllFairySoulsByEntity("dungeon")
                }

                foundFairySoulsInThisProfile =
                    FairySoulProfileCache.currentlyLoadedProfile.foundFairySouls[currentSkyblockIsland] ?: emptyList()

                if (showPath && pathTimer.update().check()) {
                    (currentIslandFairySouls - foundFairySoulsInThisProfile.toSet())
                        .takeIf { list -> list.any() && !pathFreezed }
                        ?.map(FairySoul::blockPos)
                        ?.minByOrNull(mc.thePlayer::getDistanceSq)
                        ?.let { blockPos ->
                            GlobalScope.launch {
                                fairySoulPaths = ModPathFinding(blockPos, true).findPath()
                            }
                        }
                }
            } ?: run {
                currentSkyblockIsland = currentGame.island

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

        on<PacketEvent.Sending>().filter { Nameless.hypixel.currentGame is SkyBlock }.subscribe {
            withInstance<C02PacketUseEntity>(packet) {
                if (action == C02PacketUseEntity.Action.ATTACK) {
                    val entity = getEntityFromWorld(mc.theWorld)
                    if (entity is EntityArmorStand && entity.isFairySoul()) {
                        FairySoulProfileCache.currentlyLoadedProfile.addFoundFairySoul(
                            entity.getNearestPossibleFairySoul() ?: return@subscribe
                        )
                    }
                }
            }
        }

        on<ClientChatReceivedEvent>().filter { type.toInt() != 2 && Nameless.hypixel.currentGame is SkyBlock }
            .subscribe {
                PROFILE.matchesMatcher(pureText) {
                    currentSkyBlockProfile = group("profile")
                }
                PROFILE_CHANGE.matchesMatcher(pureText) {
                    currentSkyBlockProfile = group("profile")
                }
            }
    }

    private fun EntityArmorStand.getNearestPossibleFairySoul() =
        currentIslandFairySouls.sortedBy { getDistanceSq(it.blockPos) }.find {
            getDistanceSq(it.blockPos) < 4
        }

}