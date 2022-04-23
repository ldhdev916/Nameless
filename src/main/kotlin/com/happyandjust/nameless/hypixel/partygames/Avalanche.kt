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

package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.util.*
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.timerTask

class Avalanche : PartyMiniGames {

    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val renderingCallbacks = CopyOnWriteArraySet<(Float) -> Unit>()

    override fun isEnabled() = PartyGamesHelper.avalanche

    override fun registerEventListeners() {

        on<ClientChatReceivedEvent>().filter { pureText.matches(ROUND_CHANGE) }.addSubscribe {
            renderingCallbacks.clear()
            finderTimer.schedule(timerTask {
                val slabPositions = getSlabs()
                slabPositions.forEach {
                    val aabb =
                        AxisAlignedBB(it.x.toDouble(), it.y - 3.0, it.z.toDouble(), it.x + 1.0, it.y - 1.0, it.z + 1.0)

                    renderingCallbacks.add { partialTicks ->
                        aabb.drawOutlinedBox(boxColor, partialTicks)
                    }
                }
            }, 700L)
        }

        on<RenderWorldLastEvent>().addSubscribe {
            renderingCallbacks.forEach { it(partialTicks) }
        }
    }

    private fun getSlabs() = BlockPos.getAllInBox(from, to).filter {
        mc.theWorld.getBlockAtPos(it) == Blocks.wooden_slab
    }

    companion object : PartyMiniGamesCreator {
        override val scoreboardIdentifier = "Avalanche"

        private val ROUND_CHANGE =
            "Wave \\d+ will begin in \\d+ seconds with \\d+ safe point(s)?! Find cover!".toRegex()
        private val from = BlockPos(-2406, 49, -1893)
        private val to = BlockPos(-2380, 49, -1867)
        private val finderTimer = Timer()
        private val boxColor
            get() = PartyGamesHelper.avalancheColor.rgb

        override fun createImpl() = Avalanche()
    }
}