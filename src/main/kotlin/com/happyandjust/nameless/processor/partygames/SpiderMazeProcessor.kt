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

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.PartyGameChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.block.BlockWeb
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

@OptIn(DelicateCoroutinesApi::class)
object SpiderMazeProcessor : Processor() {

    private var mazePath = emptyList<BlockPos>()
    private val mazeEnds =
        listOf(BlockPos(45, 2, 2099), BlockPos(44, 2, 2098), BlockPos(45, 2, 2098), BlockPos(44, 2, 2099))
    private val pathTimer = TickTimer.withSecond(1.5)
    override val filter = PartyGamesHelper.getFilter(this)

    init {
        request<SpecialTickEvent>().filter { pathTimer.update().check() }.subscribe {
            GlobalScope.launch {
                mazePath = mazeEnds.map {
                    async {
                        ModPathFinding(
                            it,
                            false,
                            additionalValidCheck = { pos -> mc.theWorld.getBlockAtPos(pos) !is BlockWeb }).findPath()
                    }
                }.minByOrNull { it.await().size }!!.await()
            }
        }

        request<RenderWorldLastEvent>().subscribe {
            RenderUtils.drawPath(mazePath, Color.red.rgb, partialTicks)
        }

        on<PartyGameChangeEvent>().filter { from == PartyGamesType.SPIDER_MAZE || to == PartyGamesType.SPIDER_MAZE }
            .subscribe {
                mazePath = emptyList()
            }
    }
}