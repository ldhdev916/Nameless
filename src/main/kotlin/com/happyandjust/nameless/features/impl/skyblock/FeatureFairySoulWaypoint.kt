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

import com.happyandjust.nameless.dsl.*
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
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.awt.Color
import java.util.regex.Pattern

object FeatureFairySoulWaypoint : SimpleFeature(
    Category.SKYBLOCK,
    "fairysoulwaypoint",
    "FairySoul Waypoint",
    "Renders outline box on fairysoul except the ones you've already found"
), WorldRenderListener, ServerChangeListener, ClientTickListener, KeyInputListener, PacketListener, ChatListener {

    private val PROFILE = Pattern.compile("You are playing on profile: (?<profile>\\w+).*")
    private val PROFILE_CHANGE = Pattern.compile("Your profile was changed to: (?<profile>\\w+).*")

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
    private var currentSkyBlockProfile = "Unknown"
        set(value) {
            if (field != value) {
                field = value
                FairySoulProfileCache.changeToProfileAndIfNotExistThenCreate(value)
            }
        }

    init {
        parameters["path"] = FeatureParameter(
            0,
            "fairysoulwaypoint",
            "path",
            "Fairy Soul Path Finding",
            "Show path to nearest fairysoul",
            true,
            CBoolean
        )
    }

    override fun renderWorld(partialTicks: Float) {
        if (!enabled) return
        if (Hypixel.currentGame == GameType.SKYBLOCK) {
            if (getParameterValue("path")) {
                RenderUtils.drawPath(fairySoulPaths, if (pathFreezed) freezedPathColor else Color.red.rgb, partialTicks)
            }
            if (currentIslandFairySouls.isNotEmpty()) {
                for (fairySoul in currentIslandFairySouls - foundFairySoulsInThisProfile.toSet()) {
                    RenderUtils.drawBox(fairySoul.toBlockPos().getAxisAlignedBB(), fairySoulColor, partialTicks)
                }
            }
        }
    }

    override fun tick() {
        if (enabled && Hypixel.currentGame == GameType.SKYBLOCK) {
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
                        currentIslandFairySouls
                            .filter { fairySoul -> !foundFairySoulsInThisProfile.contains(fairySoul) }
                            .takeIf { list -> list.any() && !pathFreezed }
                            ?.map(FairySoul::toBlockPos)
                            ?.minByOrNull(mc.thePlayer::getDistanceSq)
                            ?.let { blockPos ->
                                fairySoulPaths = ModPathFinding(blockPos, true).findPath()
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

        currentSkyBlockProfile = "Unknown"
    }

    override fun onKeyInput() {
        if (KeyBindingCategory.FREEZE_FAIRYSOUL_PATHS.getKeyBinding().isKeyDown) {
            pathFreezed = !pathFreezed
        }
    }

    override fun onSendingPacket(e: PacketEvent.Sending) {
        if (Hypixel.currentGame != GameType.SKYBLOCK) return
        val msg = e.packet
        if (msg is C02PacketUseEntity && msg.action == C02PacketUseEntity.Action.ATTACK) {
            val entity = msg.getEntityFromWorld(mc.theWorld)

            if (entity is EntityArmorStand && entity.isFairySoul()) {
                FairySoulProfileCache.currentlyLoadedProfile.addFoundFairySoul(
                    currentSkyblockIsland ?: return,
                    BlockPos(entity.posX, entity.posY + 2, entity.posZ)
                )
            }
        }
    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
    }

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (e.type.toInt() == 2) return
        if (Hypixel.currentGame != GameType.SKYBLOCK) return

        val msg = e.message.unformattedText.stripControlCodes().trim()

        PROFILE.matchesMatcher(msg) {
            currentSkyBlockProfile = it.group("profile")
        }
        PROFILE_CHANGE.matchesMatcher(msg) {
            currentSkyBlockProfile = it.group("profile")
        }
    }

}