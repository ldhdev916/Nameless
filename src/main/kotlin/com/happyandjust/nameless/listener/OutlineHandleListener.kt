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
import com.happyandjust.nameless.core.enums.OutlineMode
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.mixinhooks.RenderGlobalHook
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge

object OutlineHandleListener {

    var manualRendering = false
    private val outlineEntityCache = hashMapOf<Entity, Int>()
    private val changeColorEntityCache = hashMapOf<Entity, Int>()

    init {
        on<SpecialTickEvent>().subscribe {
            outlineEntityCache.clear()
            changeColorEntityCache.clear()

            for (entity in mc.theWorld.loadedEntityList) {
                val event = OutlineRenderEvent(entity)
                MinecraftForge.EVENT_BUS.post(event)

                outlineEntityCache[entity] = event.colorInfo?.color ?: continue
            }
        }
        on<RenderWorldLastEvent>().subscribe {
            for (entity in mc.theWorld.loadedEntityList) {

                changeColorEntityCache[entity]?.let {
                    manualRendering = true
                    RenderUtils.changeEntityColor(entity, it, partialTicks)
                    manualRendering = false
                }

                if (!RenderGlobalHook.canDisplayOutline() || Nameless.selectedOutlineMode == OutlineMode.BOX) {
                    outlineEntityCache[entity]?.let {
                        RenderUtils.drawOutlinedBox(entity.entityBoundingBox, it, partialTicks)
                    }
                }

            }
        }

        on<RenderLivingEvent.Specials.Pre<EntityLivingBase>>().subscribe {
            if (manualRendering) {
                isCanceled = true
            }
        }
    }

    fun getOutlineColorForEntity(entity: Entity) = outlineEntityCache[entity]
}