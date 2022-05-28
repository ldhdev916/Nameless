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

import com.happyandjust.nameless.dsl.drawFilledBox
import com.happyandjust.nameless.dsl.getAxisAlignedBB
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent

class AnvilSpleef : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val renderingCallbacks = hashSetOf<(Float) -> Unit>()

    override fun isEnabled() = PartyGamesHelper.anvil

    override fun registerEventListeners() {
        on<SpecialTickEvent>().addSubscribe {
            renderingCallbacks.clear()
            val anvils = getAnvils()
            anvils.forEach {
                val aabb = BlockPos(it.posX, 0.0, it.posZ).getAxisAlignedBB()
                renderingCallbacks.add { partialTicks ->
                    aabb.drawFilledBox(anvilColor, partialTicks)
                }
            }
        }

        on<RenderWorldLastEvent>().addSubscribe {
            renderingCallbacks.forEach { it(partialTicks) }
        }
    }

    private fun getAnvils() = mc.theWorld.loadedEntityList
        .filterIsInstance<EntityFallingBlock>()
        .filter { it.posY > 1 && it.block.block == Blocks.anvil }

    companion object {

        private val anvilColor
            get() = PartyGamesHelper.anvilColor.rgb
    }
}