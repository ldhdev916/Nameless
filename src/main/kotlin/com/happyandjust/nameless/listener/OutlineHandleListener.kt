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
import com.happyandjust.nameless.core.ColorInfo
import com.happyandjust.nameless.core.OutlineMode
import com.happyandjust.nameless.core.checkAndReplace
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.features.listener.StencilListener
import com.happyandjust.nameless.mixinhooks.RenderGlobalHook
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object OutlineHandleListener {

    var manualRendering = false
    private val outlineEntityCache = hashMapOf<Entity, Int>()
    private val changeColorEntityCache = hashMapOf<Entity, Int>()

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) return
        if (mc.theWorld == null) return

        outlineEntityCache.clear()
        changeColorEntityCache.clear()

        val stencilFeatures = FeatureRegistry.features.filterIsInstance<StencilListener>()
        val stencilProcessers = FeatureRegistry.features
            .asSequence()
            .filter { it.enabled }
            .map { it.processors.filter { entry -> entry.value() } }
            .map { it.keys }
            .flatten()
            .filterIsInstance<StencilListener>()
        val stencilListeners = stencilFeatures + stencilProcessers

        for (entity in mc.theWorld.loadedEntityList) {
            var outlineColorInfo: ColorInfo? = null
            var entityColorInfo: ColorInfo? = null

            for (stencilListener in stencilListeners) {
                stencilListener.getOutlineColor(entity)?.let {
                    outlineColorInfo = outlineColorInfo.checkAndReplace(it)
                }
                stencilListener.getEntityColor(entity)?.let {
                    entityColorInfo = entityColorInfo.checkAndReplace(it)
                }
            }

            outlineColorInfo?.let {
                outlineEntityCache[entity] = it.color
            }

            entityColorInfo?.let {
                changeColorEntityCache[entity] = it.color
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(e: RenderWorldLastEvent) {

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity == mc.renderViewEntity) continue

            changeColorEntityCache[entity]?.let {
                manualRendering = true
                RenderUtils.changeEntityColor(entity, it, e.partialTicks)
                manualRendering = false
            }


            if (!RenderGlobalHook.canDisplayOutline() || Nameless.INSTANCE.selectedOutlineMode == OutlineMode.BOX) {
                outlineEntityCache[entity]?.let {
                    RenderUtils.drawOutlinedBox(entity.entityBoundingBox, it, e.partialTicks)
                }
            }

        }
    }

    fun getOutlineColorForEntity(entity: Entity) = outlineEntityCache[entity]

    @SubscribeEvent
    fun onRenderName(e: RenderLivingEvent.Specials.Pre<EntityLivingBase>) {
        if (manualRendering) {
            e.isCanceled = true
        }
    }
}