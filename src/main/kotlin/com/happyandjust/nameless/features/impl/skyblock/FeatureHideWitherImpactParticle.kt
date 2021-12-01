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

import com.happyandjust.nameless.dsl.getSkyBlockID
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.PacketListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import kotlin.math.pow
import kotlin.math.sqrt

object FeatureHideWitherImpactParticle : SimpleFeature(
    Category.SKYBLOCK,
    "hidewitherimpactparticle",
    "Hide Wither Impact Particle",
    "Hide only wither impact's explosion particle(can be inaccurate)"
), PacketListener {

    private val SWORDS = arrayOf("VALKYRIE", "HYPERION", "ASTRAEA", "SCYLLA")
    private val distanceToPlayer: S2APacketParticles.(player: EntityPlayer) -> Double = {
        sqrt((xCoordinate - it.posX).pow(2) + (yCoordinate - it.posY).pow(2) + (zCoordinate - it.posZ).pow(2))
    }

    private fun findWitherSwordsPlayers() = mc.theWorld.playerEntities.filter { it.heldItem.getSkyBlockID() in SWORDS }

    override fun onSendingPacket(e: PacketEvent.Sending) {

    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
        if (!enabled || Hypixel.currentGame != GameType.SKYBLOCK) return
        val msg = e.packet

        if (msg is S2APacketParticles && msg.particleType == EnumParticleTypes.EXPLOSION_LARGE) {
            if (msg.isLongDistance && msg.particleArgs.isEmpty() && msg.particleCount == 8 && msg.particleSpeed == 8f) {
                val players = findWitherSwordsPlayers()
                if (players.isEmpty()) return

                if (players.minOf { msg.distanceToPlayer(it) } < 15) {
                    e.isCanceled = true
                }
            }
        }
    }
}