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

import com.happyandjust.nameless.dsl.getAxisAlignedBB
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.PartyGameChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

object AnvilSpleefProcessor : Processor(), ClientTickListener, WorldRenderListener, PartyGameChangeListener {

    var boxColor = { -1 }
    private val renderingSet = hashSetOf<EntityFallingBlock>()

    override fun tick() {
        renderingSet.clear()
        renderingSet.addAll(
            mc.theWorld.loadedEntityList
                .filterIsInstance<EntityFallingBlock>()
                .filter { it.posY > 1 && it.block.block == Blocks.anvil }
        )
    }

    override fun renderWorld(partialTicks: Float) {
        for (anvil in renderingSet) {
            val pos = BlockPos(anvil.posX, 1.0, anvil.posZ)

            RenderUtils.drawOutlinedBox(pos.getAxisAlignedBB(), boxColor(), partialTicks)
        }
    }

    override fun onPartyGameChange(from: PartyGamesType?, to: PartyGamesType?) {
        if (from == PartyGamesType.ANVIL_SPLEEF || to == PartyGamesType.ANVIL_SPLEEF) {
            renderingSet.clear()
        }
    }

}