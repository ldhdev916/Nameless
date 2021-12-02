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
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.toVec3
import com.happyandjust.nameless.dsl.withAlpha
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.utils.RenderUtils
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object WaypointListener {

    private var pathTick = 0
    private var pathFreezed = false
    val waypointInfos = arrayListOf<WaypointInfo>()

    @OptIn(DelicateCoroutinesApi::class)
    private fun createPathToPosition() {
        GlobalScope.launch {
            waypointInfos.filter { it.enabled }.forEach {
                async { it.waypointPaths = ModPathFinding(it.targetPos, it.canFly).findPath() }
            }
        }
    }

    @SubscribeEvent
    fun onTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) return
        if (mc.thePlayer == null || mc.theWorld == null) return

        if (pathFreezed) return

        pathTick = (pathTick + 1) % 20

        if (pathTick == 0) {
            createPathToPosition()
        }
    }

    @SubscribeEvent
    fun onWorldRender(e: RenderWorldLastEvent) {

        for (waypoint in waypointInfos) {
            if (!waypoint.enabled) continue
            val color = waypoint.color.rgb
            RenderUtils.drawPath(
                waypoint.waypointPaths,
                if (pathFreezed) color.withAlpha(0.5f) else color,
                e.partialTicks
            )
            RenderUtils.draw3DString(waypoint.name, waypoint.targetPos, 2.5, color, e.partialTicks)
            RenderUtils.renderBeaconBeam(waypoint.targetPos.toVec3(), color, 0.7f, e.partialTicks)
        }
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        if (Nameless.keyBindings[KeyBindingCategory.FREEZE_WAYPOINT_PATH]!!.isKeyDown) {
            pathFreezed = !pathFreezed
        }
    }

    data class WaypointInfo(var name: String, var targetPos: BlockPos, var canFly: Boolean) {
        var waypointPaths = listOf<BlockPos>()
        var enabled = true
        var color = Color.red.toChromaColor()
    }

}