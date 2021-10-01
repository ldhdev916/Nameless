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

package com.happyandjust.nameless.features.impl

import com.happyandjust.nameless.core.Point
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorItemAxe
import com.happyandjust.nameless.mixins.accessors.AccessorItemPickaxe
import com.happyandjust.nameless.textureoverlay.Overlay
import com.happyandjust.nameless.textureoverlay.impl.EBedwarsRayTraceOverlay
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.Utils
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemShears
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.min

class FeatureBedwarsRayTraceBed : OverlayFeature(
    Category.GENERAL,
    "raytracebed",
    "Bedwars Ray Trace Bed",
    "Ray trace up to your reach(3 blocks) and if there's a bed in your ray trace\nShow all keys you should press and break blocks to get bed"
), ClientTickListener, ServerChangeListener, WorldRenderListener {

    private val blackListBlock = hashSetOf(
        Blocks.air,
        Blocks.fire,
        Blocks.water,
        Blocks.flowing_water,
        Blocks.lava,
        Blocks.flowing_lava,
        Blocks.bed
    )
    private val blockToKeyName = hashMapOf<Block, String>()
    private var scanBedTick = 0
    private var scanMyBedTick = 0
    private var rayTraceTick = 0
    private val beds = hashSetOf<BlockPos>()
    private var currentRayTraceInfo: RayTraceInfo? = null
    override val overlayPoint = getOverlayConfig("bedwarsoverlay", "overlay", Overlay(Point(0, 0), 1.0))

    override fun tick() {
        if (!enabled) return
        if (Hypixel.currentGame != GameType.BEDWARS) return

        scanForBeds()

        if (beds.isEmpty()) return

        rayTraceTick = (rayTraceTick + 1) % 5 // I don't want to ray trace every tick

        if (rayTraceTick == 0) {
            val entityPlayerSP = mc.thePlayer
            val start = Vec3(entityPlayerSP.posX, entityPlayerSP.posY + entityPlayerSP.eyeHeight, entityPlayerSP.posZ)

            val vec = entityPlayerSP.lookVec * mc.playerController.blockReachDistance.toDouble()
            val w = mc.theWorld

            var end = start.add(vec)

            val collideInfo = getCollideBed(start, end)

            val collides = PriorityQueue<BlockPos>(compareBy {
                start.distanceTo(
                    Vec3(
                        it.x.toDouble(),
                        it.y.toDouble(),
                        it.z.toDouble()
                    )
                )
            })

            collideInfo?.let {
                end = it.rayTraceResult.hitVec

                val from = BlockPos(entityPlayerSP)
                val to = it.bed

                val minY = min(from.y, to.y)
                val maxY = max(from.y, to.y)

                for (pos in BlockPos.getAllInBox(BlockPos(from.x, minY - 2, from.z), BlockPos(to.x, maxY + 2, to.z))) {
                    val block = mc.theWorld.getBlockAtPos(pos)

                    if (!blackListBlock.contains(block)) {
                        block.collisionRayTrace(w, pos, start, end)?.also {
                            collides.add(pos)
                        }
                    }
                }
            }

            val blocksAtPosition = arrayListOf<Block>()

            for (collide in collides) {
                blocksAtPosition.add(w.getBlockAtPos(collide))
            }

            storeBlockToKeyName(blocksAtPosition)

            currentRayTraceInfo =
                RayTraceInfo(start, end, collideInfo?.bed, blocksAtPosition)
        }
    }

    /**
     * @return Return one of the bed that collides with your ray trace if there are multiple beds, pick the nearest one
     */
    private fun getCollideBed(start: Vec3, end: Vec3): CollideInfo? {
        val collides = PriorityQueue<CollideInfo>(compareBy { mc.thePlayer.getDistanceSq(it.bed) })
        val w = mc.theWorld
        for (bed in beds) {
            val rayTrace = w.getBlockAtPos(bed).collisionRayTrace(w, bed, start, end) ?: continue

            collides.add(CollideInfo(bed, rayTrace))
        }

        return collides.peek()
    }

    private fun scanForBeds() {
        if (beds.isEmpty()) { // if no stored bed, store for my team's bed first

            scanMyBedTick = (scanMyBedTick + 1) % 40

            if (scanMyBedTick == 0) {
                val x = mc.thePlayer.posX
                val y = mc.thePlayer.posY
                val z = mc.thePlayer.posZ

                val from = BlockPos(x - 20, y - 20, z - 20)
                val to = BlockPos(x + 20, y + 20, z + 20)
                val w = mc.theWorld

                for (pos in BlockPos.getAllInBox(from, to)) {
                    if (w.getBlockAtPos(pos) == Blocks.bed) {
                        beds.add(pos)
                        return
                    }
                }
            }

            return
        }

        scanBedTick =
            (scanBedTick + 1) % 200 // scan bed every 10 seconds, it'll never affect your performance, hopefully

        if (scanBedTick == 0) {

            val targetY = beds.toTypedArray()[0].y.toDouble()

            val entityPlayerSP = mc.thePlayer

            val x = entityPlayerSP.posX
            val z = entityPlayerSP.posZ

            // all beds have same Y
            val from =
                BlockPos(x - 100, targetY, z - 100)
            val to =
                BlockPos(x + 100, targetY, z + 100)

            val world = mc.theWorld

            for (pos in BlockPos.getAllInBox(from, to)) {
                if (world.getBlockAtPos(pos) == Blocks.bed) {
                    beds.add(pos)
                }
            }
        }
    }

    override fun onServerChange(server: String) {
        beds.clear()
        currentRayTraceInfo = null
    }

    override fun renderWorld(partialTicks: Float) {
        currentRayTraceInfo?.let {
            it.bedHit?.let { bed ->
                RenderUtils.drawBox(bed.getAxisAlignedBB(), 0x80FF0000.toInt(), partialTicks)
            }
        }
    }

    class RayTraceInfo(val from: Vec3, val to: Vec3, val bedHit: BlockPos?, val collideExceptBed: List<Block>)

    class CollideInfo(val bed: BlockPos, val rayTraceResult: MovingObjectPosition)

    /**
     * Contains all possible blocks. maybe
     */
    private fun getBlockRequirement(block: Block): (Item) -> Boolean {
        if (AccessorItemAxe.getEFFECTIVE_ON().contains(block)) return { it is ItemAxe }
        if (AccessorItemPickaxe.getEFFECTIVE_ON().contains(block)) return { it is ItemPickaxe }
        when (block) {
            Blocks.wool -> return { it is ItemShears }
            Blocks.stained_hardened_clay -> return { it is ItemPickaxe }
            Blocks.stained_glass -> return { false }
            Blocks.end_stone -> return { it is ItemPickaxe }
            Blocks.obsidian -> return { it is ItemPickaxe }
            else -> return { false }
        }
    }

    override fun getRelocatablePanel() = EBedwarsRayTraceOverlay(overlayPoint.value)

    private fun storeBlockToKeyName(list: Iterable<Block>) {
        blockToKeyName.clear()
        val map = Utils.getKeyBindingNameInEverySlot()

        val inventory = mc.thePlayer.inventory

        label@ for (block in list) {

            val requirement = getBlockRequirement(block)

            for ((_, inventorySlotInfo) in map) {
                if (requirement(inventorySlotInfo.itemStack?.item ?: continue)) {
                    blockToKeyName[block] = inventorySlotInfo.keyName
                    continue@label
                }
            }

            blockToKeyName[block] = map[inventory.currentItem]!!.keyName
        }
    }


    override fun renderOverlay(partialTicks: Float) {
        if (!enabled) return
        if (Hypixel.currentGame != GameType.BEDWARS) return

        currentRayTraceInfo?.let {
            val overlay = overlayPoint.value

            matrix {
                translate(overlay.point.x, overlay.point.y, 0)
                scale(overlay.scale, overlay.scale, 1.0)
                var y = 0

                try {
                    for (collideBlock in it.collideExceptBed) {
                        val text = blockToKeyName[collideBlock] ?: "NULL"
                        mc.fontRendererObj.drawString(text, 0, y, Color.red.rgb)
                        y += mc.fontRendererObj.FONT_HEIGHT
                    }
                } catch (e: NullPointerException) { // somewhat ItemStack#getDisplayName throwing Exception
                    e.printStackTrace()
                }
            }
        }
    }

}