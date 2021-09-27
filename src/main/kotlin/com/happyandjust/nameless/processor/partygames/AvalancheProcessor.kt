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

package com.happyandjust.nameless.processor.partygames

import com.happyandjust.nameless.devqol.getBlockAtPos
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.stripControlCodes
import com.happyandjust.nameless.features.listener.ChatListener
import com.happyandjust.nameless.features.listener.PartyGameChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.util.*
import java.util.regex.Pattern
import kotlin.concurrent.timerTask

object AvalancheProcessor : Processor(), ChatListener, PartyGameChangeListener, WorldRenderListener {

    var boxColor = { -1 }
    private val ROUND_CHANGE =
        Pattern.compile("Wave \\d+ will begin in \\d+ seconds with \\d+ safe point(s)?! Find cover!")
    private val slabs = arrayListOf<AxisAlignedBB>()
    private val timer = Timer()

    private val minPos = BlockPos(-2406, 49, -1893)
    private val maxPos = BlockPos(-2380, 49, -1867)

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (e.message.unformattedText.stripControlCodes().matches(ROUND_CHANGE.toRegex())) {
            slabs.clear()
            // slabs aren't generated instantly
            timer.schedule(timerTask { findSlabs() }, 700L)

        }
    }

    private fun findSlabs() {
        for (pos in BlockPos.getAllInBox(minPos, maxPos)) {
            if (mc.theWorld.getBlockAtPos(pos) == Blocks.wooden_slab) {
                slabs.add(
                    AxisAlignedBB(
                        pos.x.toDouble(),
                        pos.y - 3.0,
                        pos.z.toDouble(),
                        pos.x + 1.0,
                        pos.y - 1.0,
                        pos.z + 1.0
                    )
                )
            }
        }
    }

    override fun onPartyGameChange(from: PartyGamesType?, to: PartyGamesType?) {
        if (from == PartyGamesType.AVALANCHE || to == PartyGamesType.AVALANCHE) {
            slabs.clear()
        }
    }

    override fun renderWorld(partialTicks: Float) {
        for (slab in slabs) {
            RenderUtils.drawOutlinedBox(slab, boxColor(), partialTicks)
        }
    }
}