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

import com.happyandjust.nameless.core.info.InventorySlotInfo.Companion.getSlotsFromInventory
import com.happyandjust.nameless.core.value.Pos
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

class JigsawRush : PartyMiniGames {

    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private var currentCanvas: Canvas? = null
    private val renderingCallbacks = hashSetOf<(Float) -> Unit>()

    override fun isEnabled() = PartyGamesHelper.jigsaw

    override fun registerEventListeners() {
        on<SpecialTickEvent>().filter { renderingCallbacks.isEmpty() }.addSubscribe {
            val currentCanvas = currentCanvas ?: findCanvas() ?: return@addSubscribe
            if (!isGameStarted()) return@addSubscribe

            val inventorySlots = mc.thePlayer.getSlotsFromInventory()
                .filter { it.itemStack != null }
                .associate { it.itemStack!!.item to it.keyName }

            if (inventorySlots.size != 9) return@addSubscribe

            val drawOffset = currentCanvas.facingToCanvas.opposite

            Pos.values.forEach {
                sendDebugMessage("-------------------------------------")
                sendDebugMessage(it)

                val positionOfGameCanvas = gameCanvas.getValue(it)
                sendDebugMessage("Game Canvas: $positionOfGameCanvas")

                val block = mc.theWorld.getBlockAtPos(positionOfGameCanvas)
                sendDebugMessage("Game Canvas Block: $block")

                val positionOfMyCanvas = currentCanvas.canvasBlocks.getValue(it)
                sendDebugMessage("My Canvas: $positionOfMyCanvas")

                val keyName = inventorySlots.getValue(Item.getItemFromBlock(block))
                sendDebugMessage("Key Name: $keyName")
                sendDebugMessage("-------------------------------------")

                val vec3 = with(positionOfMyCanvas) {
                    with(drawOffset) {
                        Vec3(
                            x + 0.5 + frontOffsetX * 0.5,
                            y + 0.5,
                            z + 0.5 + frontOffsetZ * 0.5
                        )
                    }
                }
                sendDebugMessage("Drawing Position: $vec3")

                renderingCallbacks.add { partialTicks ->
                    vec3.drawString(keyName, 0.5, Color.red.rgb, partialTicks)
                }
            }
        }

        on<RenderWorldLastEvent>().addSubscribe {
            renderingCallbacks.forEach { it(partialTicks) }
        }
    }

    private fun isGameStarted() = mc.theWorld.getBlockAtPos(gameCanvas.getValue(Pos.CENTER)) != Blocks.wool

    private fun findCanvas(): Canvas? {
        val base = with(mc.thePlayer) { BlockPos(posX, 2.0, posZ) }
        if (mc.theWorld.getBlockAtPos(base.down()) != Blocks.wool) return null

        expandingFacings.forEach { facing ->
            for (i in 0 until 5) {
                val identifyPosition = base.offset(facing, i)
                when (mc.theWorld.getBlockAtPos(identifyPosition)) {
                    Blocks.oak_fence_gate, Blocks.cobblestone_wall -> break
                    Blocks.oak_stairs -> {
                        val center = identifyPosition.offset(facing).up(2)
                        return processCanvasData(center, facing).also { sendDebugMessage(it) }
                    }
                }
            }
        }
        return null
    }

    private fun processCanvasData(center: BlockPos, facing: EnumFacing): Canvas {
        val rotateY = facing.rotateY()
        val canvasBlocks = Pos.values.withIndex().associate { (index, pos) ->
            val facingOffset = (index % 3) - 1
            val yOffset = (index / 3) - 1

            pos to center.offset(rotateY, facingOffset).down(yOffset)
        }
        return Canvas(canvasBlocks, facing)
    }

    private data class Canvas(val canvasBlocks: Map<Pos, BlockPos>, val facingToCanvas: EnumFacing)

    companion object : PartyMiniGamesCreator {
        private val gameCanvas = Pos.values.withIndex().associate { (index, pos) ->
            val yOffset = 3 * ((index / 3) - 1)
            val zOffset = 3 * ((index % 3) - 1)

            pos to BlockPos(226, 11 - yOffset, 1817 - zOffset)
        }

        private val expandingFacings = EnumFacing.VALUES.filter { it.axis != EnumFacing.Axis.Y }

        override fun createImpl() = JigsawRush()

        override val scoreboardIdentifier = "Jigsaw Rush"
    }
}