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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.devqol.getAxisAlignedBB
import com.happyandjust.nameless.devqol.isFairySoul
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.*
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.fairysoul.FairySoulProfileCache
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.serialization.TypeRegistry
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import java.awt.Color

class FeatureFairySoulWaypoint : SimpleFeature(
    Category.SKYBLOCK,
    "fairysoulwaypoint",
    "FairySoul Waypoint",
    "Renders outline box on fairysoul except the ones you've already found\nTo collect found fairysouls data, we need your profile type /fairysoulprofile for help\nas a default, we pre-created profile named 'default'"
), WorldRenderListener, ServerChangeListener, ClientTickListener, KeyInputListener, PacketListener {

    var currentSkyblockIsland: String? = null
    private var fairySoulPaths = listOf<BlockPos>()
    private var currentIslandFairySouls = listOf<FairySoul>()
    private val fairySoulColor = Color(0, 0, 255, 134).rgb
    private val freezedPathColor = Color(95, 95, 229).rgb
    private var pathFreezed = false
    private val REFRESH_TICK = 20
    private var pathTick = 0
    private var foundFairySoulsInThisProfile = emptyList<FairySoul>()
    private var dungeonFairySoulScanTick = 0

    init {
        parameters["path"] = FeatureParameter(
            0,
            "fairysoulwaypoint",
            "path",
            "Fairy Soul Path Finding",
            "Show path to nearest fairysoul",
            true,
            TypeRegistry.getConverterByClass(Boolean::class)
        )
    }

    override fun renderWorld(partialTicks: Float) {
        if (!enabled) return
        if (Hypixel.currentGame == GameType.SKYBLOCK) {
            if (getParameterValue("path") && fairySoulPaths.isNotEmpty()) {
                RenderUtils.drawPath(fairySoulPaths, if (pathFreezed) freezedPathColor else Color.red.rgb, partialTicks)
            }
            if (currentIslandFairySouls.isNotEmpty()) {
                for (fairySoul in currentIslandFairySouls) {
                    if (foundFairySoulsInThisProfile.contains(fairySoul)) continue

                    RenderUtils.drawBox(fairySoul.toBlockPos().getAxisAlignedBB(), fairySoulColor, partialTicks)
                }
            }
        }
    }

    override fun tick() {
        if (Hypixel.currentGame == GameType.SKYBLOCK) {
            currentSkyblockIsland?.let {

                if (it == "dungeon") {
                    dungeonFairySoulScanTick = (dungeonFairySoulScanTick + 1) % 20

                    if (dungeonFairySoulScanTick == 0) {
                        currentIslandFairySouls = SkyblockUtils.getAllFairySoulsByEntity("dungeon")
                    }
                }

                foundFairySoulsInThisProfile =
                    FairySoulProfileCache.currentlyLoadedProfile.foundFairySouls[currentSkyblockIsland] ?: emptyList()

                if (getParameterValue("path")) {
                    pathTick = (pathTick + 1) % REFRESH_TICK

                    if (pathTick == 0) {
                        currentIslandFairySouls.filter { fairySoul -> !foundFairySoulsInThisProfile.contains(fairySoul) }
                            .apply {
                                if (isNotEmpty() && !pathFreezed) {
                                    threadPool.execute {
                                        fairySoulPaths = ModPathFinding(
                                            sortedBy { fairySoul -> mc.thePlayer.getDistanceSq(fairySoul.toBlockPos()) }[0].toBlockPos(),
                                            true
                                        ).findPath().get()
                                    }
                                }
                            }
                    }
                }
            } ?: run {
                currentSkyblockIsland = Hypixel.getProperty(PropertyKey.ISLAND)

                currentSkyblockIsland?.let {
                    currentIslandFairySouls = SkyblockUtils.getAllFairySoulsInWorld(it)
                }

            }
        }
    }

    override fun onServerChange(server: String) {
        currentSkyblockIsland = null
    }

    override fun onKeyInput() {
        if (Nameless.INSTANCE.keyBindings[KeyBindingCategory.FREEZE_FAIRYSOUL_PATHS]!!.isKeyDown) {
            pathFreezed = !pathFreezed
        }
    }

    override fun onSendingPacket(e: PacketEvent.Sending) {
        if (Hypixel.currentGame != GameType.SKYBLOCK) return
        FairySoulProfileCache.currentlyLoadedProfile.let {
            val msg = e.packet
            if (msg is C02PacketUseEntity && msg.action == C02PacketUseEntity.Action.ATTACK) {
                val entity = msg.getEntityFromWorld(mc.theWorld)

                if (entity is EntityArmorStand && entity.isFairySoul()) {
                    it.addFoundFairySoul(
                        currentSkyblockIsland ?: return,
                        BlockPos(entity.posX, entity.posY + 2, entity.posZ)
                    )
                }
            }
        }
    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
    }

}
