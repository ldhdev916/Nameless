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

package com.happyandjust.nameless.listener

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.devqol.getAxisAlignedBB
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import java.util.concurrent.Executors

object WaypointListener {

    var currentWaypointInfo: WaypointInfo? = null
        set(value) {
            field = value

            value?.let {
                createPathToPosition(it.targetPos)
            }
        }
    private var pathTick = 0
    private val threadPool = Executors.newSingleThreadExecutor()
    private var pathFreezed = false
    private val freezedPathColor = Color(95, 95, 229).rgb

    private fun createPathToPosition(pos: BlockPos) {
        threadPool.execute {
            currentWaypointInfo?.let {
                it.waypointPaths = ModPathFinding(pos, it.canFly).findPath().get()
            }
        }
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) return
        if (mc.thePlayer == null || mc.theWorld == null) return

        if (pathFreezed) return

        currentWaypointInfo?.let {
            pathTick = (pathTick + 1) % 20

            if (pathTick == 0) {
                createPathToPosition(it.targetPos)
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(e: RenderWorldLastEvent) {

        currentWaypointInfo?.let {
            try {
                RenderUtils.drawPath(
                    it.waypointPaths,
                    if (pathFreezed) freezedPathColor else Color.red.rgb,
                    e.partialTicks
                )
            } catch (ignored: ConcurrentModificationException) {

            }
            RenderUtils.drawBox(it.targetPos.getAxisAlignedBB(), 0x4000FF00, e.partialTicks)
        }
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        if (Nameless.INSTANCE.keyBindings[KeyBindingCategory.FREEZE_WAYPOINT_PATH]!!.isKeyDown) {
            pathFreezed = !pathFreezed
        }
    }

    data class WaypointInfo(val targetPos: BlockPos, val canFly: Boolean) {
        var waypointPaths = listOf<BlockPos>()
    }

}