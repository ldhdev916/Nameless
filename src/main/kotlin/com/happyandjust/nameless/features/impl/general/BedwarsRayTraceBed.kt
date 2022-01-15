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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorItemAxe
import com.happyandjust.nameless.mixins.accessors.AccessorItemPickaxe
import com.happyandjust.nameless.serialization.converters.COverlay
import com.happyandjust.nameless.utils.RenderUtils
import com.happyandjust.nameless.utils.Utils
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import net.minecraft.block.Block
import net.minecraft.block.BlockBed
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemPickaxe
import net.minecraft.item.ItemShears
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import java.util.*
import kotlin.math.max
import kotlin.math.min

object BedwarsRayTraceBed : OverlayFeature(
    Category.GENERAL,
    "raytracebed",
    "Bedwars Ray Trace Bed",
    "Ray trace up to your reach(3 blocks) and if there's a bed in your ray trace, Show all keys you should press and break blocks to get bed"
) {

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
    private val scanBedTimer = TickTimer.withSecond(10)
    private val scanMyBedTimer = TickTimer.withSecond(2)
    private val rayTraceTimer = TickTimer(5)
    private val beds = hashSetOf<BlockPos>()
    private var currentRayTraceInfo: RayTraceInfo? = null
    override var overlayPoint by ConfigValue("bedwarsoverlay", "overlay", Overlay.DEFAULT, COverlay)

    init {
        on<SpecialTickEvent>().filter { enabled && Hypixel.currentGame == GameType.BEDWARS }.subscribe {
            scanForBeds()

            if (beds.isEmpty()) return@subscribe
            if (rayTraceTimer.update().check()) {
                val start = Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ)

                val vec = mc.thePlayer.lookVec * mc.playerController.blockReachDistance.toDouble()

                var end = start.add(vec)

                val collideInfo = getCollideBed(start, end)

                val collides = PriorityQueue<BlockPos>(compareBy {
                    start.distanceTo(it.toVec3())
                })

                collideInfo?.let {
                    end = it.rayTraceResult.hitVec

                    val from = BlockPos(mc.thePlayer)
                    val to = it.bed

                    val minY = min(from.y, to.y)
                    val maxY = max(from.y, to.y)

                    for (pos in BlockPos.getAllInBox(
                        BlockPos(from.x, minY - 2, from.z),
                        BlockPos(to.x, maxY + 2, to.z)
                    )) {
                        val block = mc.theWorld.getBlockAtPos(pos)

                        if (block !in blackListBlock) {
                            block.collisionRayTrace(mc.theWorld, pos, start, end)?.let {
                                collides.add(pos)
                            }
                        }
                    }
                }

                val blocksAtPosition = collides.map { mc.theWorld.getBlockAtPos(it) }

                storeBlockToKeyName(blocksAtPosition)

                currentRayTraceInfo = RayTraceInfo(start, end, collideInfo?.bed, blocksAtPosition)
            }
        }
    }

    /**
     * @return Return one of the bed that collides with your ray trace if there are multiple beds, pick the nearest one
     */
    private fun getCollideBed(start: Vec3, end: Vec3): CollideInfo? {
        val collides = PriorityQueue<CollideInfo>(compareBy { mc.thePlayer.getDistanceSq(it.bed) })
        for (bed in beds) {
            val rayTrace = mc.theWorld.getBlockAtPos(bed).collisionRayTrace(mc.theWorld, bed, start, end) ?: continue

            collides.add(CollideInfo(bed, rayTrace))
        }

        return collides.peek()
    }

    private fun scanForBeds() {
        if (beds.isEmpty()) { // if no stored bed, store for my team's bed first
            if (scanMyBedTimer.update().check()) {
                val x = mc.thePlayer.posX
                val y = mc.thePlayer.posY
                val z = mc.thePlayer.posZ

                val from = BlockPos(x - 20, y - 20, z - 20)
                val to = BlockPos(x + 20, y + 20, z + 20)

                beds.addAll(BlockPos.getAllInBox(from, to).filter { mc.theWorld.getBlockAtPos(it) is BlockBed })
            }

            return
        }

        if (scanBedTimer.update().check()) {

            val targetY = beds.first().y.toDouble()

            val x = mc.thePlayer.posX
            val z = mc.thePlayer.posZ

            // all beds have same Y
            val from =
                BlockPos(x - 100, targetY, z - 100)
            val to =
                BlockPos(x + 100, targetY, z + 100)

            beds.addAll(BlockPos.getAllInBox(from, to).filter { mc.theWorld.getBlockAtPos(it) is BlockBed })
        }
    }

    init {
        on<HypixelServerChangeEvent>().subscribe {
            beds.clear()
            currentRayTraceInfo = null
        }
        on<RenderWorldLastEvent>().subscribe {
            currentRayTraceInfo?.bedHit?.let {
                RenderUtils.drawBox(it.getAxisAlignedBB(), 0x80FF0000.toInt(), partialTicks)
            }
        }
    }

    class RayTraceInfo(val from: Vec3, val to: Vec3, val bedHit: BlockPos?, val collideExceptBed: List<Block>)

    class CollideInfo(val bed: BlockPos, val rayTraceResult: MovingObjectPosition)

    /**
     * Contains all possible blocks. maybe
     */
    private fun getBlockRequirement(block: Block): (Item?) -> Boolean {
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

    private fun storeBlockToKeyName(list: Iterable<Block>) {
        blockToKeyName.clear()
        val map = Utils.getKeyBindingNameInEverySlot()

        val inventory = mc.thePlayer.inventory

        blockToKeyName.putAll(
            list.associateWith { block ->
                val requirement = getBlockRequirement(block)
                (map.values.firstOrNull { requirement(it.itemStack?.item) } ?: map[inventory.currentItem]!!).keyName
            }
        )
    }

    override fun shouldDisplayInRelocateGui(): Boolean {
        return enabled && Hypixel.currentGame == GameType.BEDWARS
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (!enabled) return
        if (Hypixel.currentGame != GameType.BEDWARS) return

        currentRayTraceInfo?.let {
            matrix {
                setup(overlayPoint)
                var y = 0

                for (collideBlock in it.collideExceptBed) {
                    val text = blockToKeyName[collideBlock] ?: "NULL"
                    mc.fontRendererObj.drawString(text, 0, y, Color.red.rgb)
                    y += mc.fontRendererObj.FONT_HEIGHT
                }

            }
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        val container = UIContainer().constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        repeat(5) {
            UIText((it + 1).toString()).constrain {
                y = SiblingConstraint()

                textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()

                color = Color.red.constraint
            } childOf container
        }

        return container
    }

    private operator fun Vec3.times(m: Double) = Vec3(xCoord * m, yCoord * m, zCoord * m)

}