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

import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.PartyGameChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.PartyGamesType
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import java.awt.Color

object JigsawRushProcessor : Processor(), ClientTickListener, PartyGameChangeListener, WorldRenderListener {

    private val canvas = hashMapOf<Pos, BlockPos>().apply {
        val pos = BlockPos(226, 14, 1820)

        put(Pos.TOP_LEFT, pos)
        put(Pos.TOP_CENTER, pos.add(0, 0, -3))
        put(Pos.TOP_RIGHT, pos.add(0, 0, -6))

        put(Pos.LEFT, pos.add(0, -3, 0))
        put(Pos.CENTER, pos.add(0, -3, -3))
        put(Pos.RIGHT, pos.add(0, -3, -6))

        put(Pos.BOTTOM_LEFT, pos.add(0, -6, 0))
        put(Pos.BOTTOM_CENTER, pos.add(0, -6, -3))
        put(Pos.BOTTOM_RIGHT, pos.add(0, -6, -6))

    }

    private val drawInfos = arrayListOf<DrawInfo>()
    private val expandFacings = arrayOf(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH)
    private var myCanvas: Canvas? = null

    override fun tick() {
        if (drawInfos.isEmpty()) {

            val myCanvas = this.myCanvas ?: run {
                findMyCanvas()
                return
            }

            if (mc.theWorld.getBlockAtPos(canvas[Pos.TOP_LEFT]!!) == Blocks.wool) return

            val itemKeyBindingMap = Utils.getKeyBindingNameInEverySlot().values
                .filter { it.itemStack != null }
                .associate { it.itemStack!!.item to it.keyName }

            if (itemKeyBindingMap.size != 9) return

            val opposite = myCanvas.playerFacingToCanvas.opposite

            for (pos in Pos.v) {
                val block = mc.theWorld.getBlockAtPos(canvas[pos]!!)

                val myCanvasPosition = myCanvas.canvas[pos]!!

                val vec3 = Vec3(
                    myCanvasPosition.x + 0.5 + (opposite.frontOffsetX * 0.5),
                    myCanvasPosition.y + 0.5,
                    myCanvasPosition.z + 0.5 + (opposite.frontOffsetZ * 0.5)
                )

                val keyName = itemKeyBindingMap[Item.getItemFromBlock(block)]!!

                drawInfos.add(DrawInfo(vec3, keyName))
            }
        }
    }

    private fun findMyCanvas(): EnumFacing? {

        val expandPos = BlockPos(mc.thePlayer.posX, 2.0, mc.thePlayer.posZ)

        if (mc.theWorld.getBlockAtPos(expandPos.down()) != Blocks.wool) return null

        var playerFacingToCanvas: EnumFacing? = null
        var center: BlockPos? = null

        for (expandFacing in expandFacings) {
            repeat(5) {
                when (mc.theWorld.getBlockAtPos(expandPos.offset(expandFacing, it))) {
                    Blocks.oak_fence_gate -> {
                        return@repeat
                    }
                    Blocks.cobblestone_wall -> {
                        return@repeat
                    }
                    Blocks.oak_stairs -> {
                        playerFacingToCanvas = expandFacing
                        center = expandPos.offset(expandFacing, it + 1).up(2)
                        return@repeat
                    }
                }
            }
        }
        playerFacingToCanvas ?: return null
        center ?: return null

        val myCanvas = getCanvasByFacing(center!!, playerFacingToCanvas!!)
        this.myCanvas = Canvas(myCanvas, playerFacingToCanvas!!)

        return playerFacingToCanvas
    }

    private fun getCanvasByFacing(center: BlockPos, facing: EnumFacing): HashMap<Pos, BlockPos> {
        val rotateCCW = facing.rotateYCCW()

        val hashMap = hashMapOf<Pos, BlockPos>()

        center.apply {

            val left = offset(rotateCCW)
            val right = offset(rotateCCW, -1)

            hashMap[Pos.TOP_LEFT] = left.up()
            hashMap[Pos.TOP_CENTER] = up()
            hashMap[Pos.TOP_RIGHT] = right.up()


            hashMap[Pos.LEFT] = left
            hashMap[Pos.CENTER] = this
            hashMap[Pos.RIGHT] = right

            hashMap[Pos.BOTTOM_LEFT] = left.down()
            hashMap[Pos.BOTTOM_CENTER] = down()
            hashMap[Pos.BOTTOM_RIGHT] = right.down()
        }

        return hashMap
    }

    override fun renderWorld(partialTicks: Float) {
        if (drawInfos.isNotEmpty()) {
            for (drawInfo in drawInfos) {
                RenderUtils.draw3DString(drawInfo.keyBindingName, drawInfo.vec3, 0.5, Color.red.rgb, partialTicks)
            }
        }
    }

    private data class Canvas(val canvas: HashMap<Pos, BlockPos>, val playerFacingToCanvas: EnumFacing)

    private data class DrawInfo(val vec3: Vec3, val keyBindingName: String)

    override fun onPartyGameChange(from: PartyGamesType?, to: PartyGamesType?) {
        if (from == PartyGamesType.JIGSAW_RUSH || to == PartyGamesType.JIGSAW_RUSH) {
            drawInfos.clear()
            myCanvas = null
        }
    }

    enum class Pos {
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,

        LEFT,
        CENTER,
        RIGHT,

        BOTTOM_LEFT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT;

        companion object {
            val v = values()
        }
    }
}