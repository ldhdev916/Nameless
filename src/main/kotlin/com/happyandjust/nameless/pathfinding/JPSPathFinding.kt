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

package com.happyandjust.nameless.pathfinding

import com.happyandjust.nameless.dsl.mc
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.util.*
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class JPSPathFinding(private val from: BlockPos, private val to: BlockPos, private val timeout: Long = 1000) {

    private val nodeMap = hashMapOf<Coord, Node>()
    private val open = PriorityQueue<Node>(compareBy { it.f })
    private val blockedMap = hashMapOf<BlockPos, Boolean>()

    private val world = mc.theWorld
    private val paths = arrayListOf<BlockPos>()
    private val yRange = 1..max(to.y, from.y)
    private val playerWidth = 0.3

    private fun isBlocked(x: Int, y: Int, z: Int): Boolean {
        val pos = BlockPos(x, y, z)
        return blockedMap.getOrPut(pos) {
            if (y !in yRange) return@getOrPut true
            if (!world.isBlockLoaded(pos, false)) return@getOrPut true
            val bb = AxisAlignedBB.fromBounds(
                x + 0.5 - playerWidth,
                y.toDouble(),
                z + 0.5 - playerWidth,
                x + 0.5 + playerWidth,
                y + 1.8,
                z + 0.5 + playerWidth
            )
            return world.getCollidingBoundingBoxes(mc.thePlayer, bb).isNotEmpty()
        }
    }

    private fun openNode(x: Int, y: Int, z: Int) = nodeMap.getOrPut(Triple(x, y, z)) { Node(x, y, z) }


    private fun addNode(parent: Node, child: Node) = child.apply {
        val newG = parent.g + distTo(parent)

        if (newG < g) {
            open.remove(this)

            g = newG
            if (h == -1.0) h = distTo(to.x, to.y, to.z)
            this.parent = parent

            open.add(this)
        }
    }

    fun findPath() = paths.ifEmpty {
        val start = openNode(from.x, from.y, from.z).apply {
            g = 0.0
            h = distTo(to.x, to.y, to.z)
        }

        val endTime = System.currentTimeMillis() + timeout

        open.add(start)
        var end: Node? = null

        while (open.isNotEmpty()) {
            if (System.currentTimeMillis() > endTime) break
            val node = open.poll()

            if (node.h < (end?.h ?: Double.MAX_VALUE)) {
                end = node
            }
            if (node.x == to.x && node.y == to.y && node.z == to.z) {
                break
            }

            for (neighbor in node.getNeighbors()) {
                val jumpPoint =
                    expand(node.x, node.y, node.z, neighbor.x - node.x, neighbor.y - node.y, neighbor.z - node.z)

                if (jumpPoint?.closed == false) {
                    addNode(node, jumpPoint)
                }
            }
        }
        while (end != null) {
            paths.add(BlockPos(end.x, end.y, end.z))
            end = end.parent
        }

        paths.asReversed()
    }

    private fun Node.getNeighbors(): Set<Node> = buildSet {
        val prev = parent ?: this@getNeighbors

        val dx = (x - prev.x).coerceIn(-1, 1)
        val dy = (y - prev.y).coerceIn(-1, 1)
        val dz = (z - prev.z).coerceIn(-1, 1)

        when (abs(dx) + abs(dy) + abs(dz)) {
            0 -> {
                // add all directions
                for (i in -1..1) {
                    for (j in -1..1) {
                        for (k in -1..1) {
                            if (i == 0 && j == 0 && k == 0) continue
                            add(openNode(x + i, y + j, z + k))
                        }
                    }
                }
            }
            1 -> { // two of (dx, dy, dz) are 0
                add(openNode(x + dx, y + dy, z + dz)) // keep its direction (natural neighbor)

                for (i in -1..1) {
                    for (j in -1..1) {
                        if (i == 0 && j == 0) continue
                        // check if diagonal is blocked (diagonal from parent) if blocked, (diagonal + (dx, dy, dz) is forced neighbor
                        add(
                            when {
                                dx != 0 && isBlocked(x, y + i, z + j) -> openNode(x + dx, y + i, z + j)
                                dy != 0 && isBlocked(x + i, y, z + j) -> openNode(x + i, y + dy, z + j)
                                dz != 0 && isBlocked(x + i, y + j, z) -> openNode(x + i, y + j, z + dz)
                                else -> continue
                            }
                        )
                    }
                }
            }
            2 -> {
                //natural neighbors

                if (dx != 0) {
                    add(openNode(x + dx, y, z))
                }
                if (dy != 0) {
                    add(openNode(x, y + dy, z))
                }
                if (dz != 0) {
                    add(openNode(x, y, z + dz))
                }
                add(openNode(x + dx, y + dy, z + dz)) // keep its direction
                val loop = arrayOf(-1, 1)

                //check forced neighbors

                when {
                    dx == 0 -> {
                        addAll(loop.filter { isBlocked(x + it, y, z) }.map { openNode(x + it, y + dy, z + dz) })

                        if (isBlocked(x, y - dy, z)) {
                            add(openNode(x, y - dy, z + dz))

                            addAll(loop.filter { isBlocked(x + it, y - dy, z) }
                                .map { openNode(x + it, y - dy, z + dz) })
                        }

                        if (isBlocked(x, y, z - dz)) {
                            add(openNode(x, y + dy, z - dz))

                            addAll(loop.filter { isBlocked(x + it, y, z - dz) }
                                .map { openNode(x + it, y + dy, z - dz) })
                        }
                    }
                    dy == 0 -> {
                        addAll(loop.filter { isBlocked(x, y + it, z) }.map { openNode(x + dx, y + it, z + dz) })

                        if (isBlocked(x - dx, y, z)) {
                            add(openNode(x - dx, y, z + dz))

                            addAll(loop.filter { isBlocked(x - dx, y + it, z) }
                                .map { openNode(x - dx, y + it, z + dz) })
                        }

                        if (isBlocked(x, y, z - dz)) {
                            add(openNode(x + dx, y, z - dz))

                            addAll(loop.filter { isBlocked(x, y + it, z - dz) }
                                .map { openNode(x + dx, y + it, z - dz) })
                        }
                    }
                    dz == 0 -> {
                        addAll(loop.filter { isBlocked(x, y, z + it) }.map { openNode(x + dx, y + dy, z + it) })

                        if (isBlocked(x - dx, y, z)) {
                            add(openNode(x - dx, y + dy, z))

                            addAll(loop.filter { isBlocked(x - dx, y, z + it) }
                                .map { openNode(x - dx, y + dy, z + it) })
                        }

                        if (isBlocked(x, y - dy, z)) {
                            add(openNode(x + dx, y - dy, z))

                            addAll(loop.filter { isBlocked(x, y - dy, z + it) }
                                .map { openNode(x + dx, y - dy, z + it) })
                        }
                    }
                }
            }
            3 -> {
                // natural neighbors

                add(openNode(x, y + dy, z))
                add(openNode(x + dx, y, z))
                add(openNode(x, y, z + dz))

                add(openNode(x + dx, y, z + dz))
                add(openNode(x, y + dy, z + dz))
                add(openNode(x + dx, y + dy, z))

                add(openNode(x + dx, y + dy, z + dz)) // keep its direction

                // forced neighbors

                if (isBlocked(x, y - dy, z)) {
                    add(openNode(x + dx, y - dy, z + dz))

                    if (isBlocked(x - dx, y - dy, z)) {
                        add(openNode(x, y - dy, z + dz))
                    }
                    if (isBlocked(x, y - dy, z - dz)) {
                        add(openNode(x + dx, y - dy, z))
                    }
                }

                if (isBlocked(x, y, z - dz)) {
                    add(openNode(x + dx, y + dy, z - dz))

                    if (isBlocked(x - dx, y, z - dz)) {
                        add(openNode(x, y + dy, z - dz))
                    }
                    if (isBlocked(x, y - dy, z - dz)) {
                        add(openNode(x + dx, y, z - dz))
                    }
                }

                if (isBlocked(x - dx, y, z)) {
                    add(openNode(x - dx, y + dy, z + dz))

                    if (isBlocked(x - dx, y, z - dz)) {
                        add(openNode(x - dx, y + dy, z))
                    }
                    if (isBlocked(x - dx, y - dy, z)) {
                        add(openNode(x - dx, y, z + dz))
                    }
                }
            }
        }
    }

    private fun expand(x: Int, y: Int, z: Int, dx: Int, dy: Int, dz: Int): Node? {
        var x = x
        var y = y
        var z = z

        val move = abs(dx) + abs(dy) + abs(dz)

        while (true) {
            val newX = x + dx
            val newY = y + dy
            val newZ = z + dz

            if (isBlocked(newX, newY, newZ)) return null

            if (newX == to.x && newY == to.y && newZ == to.z) return openNode(newX, newY, newZ)

            val openCurrent = { openNode(newX, newY, newZ) }
            // find when forced neighbors
            when (move) {
                1 -> {
                    for (i in -1..1) {
                        for (j in -1..1) {
                            if (i == 0 && j == 0) continue

                            return when {
                                dx != 0 &&
                                        isBlocked(newX, newY + i, newZ + j) &&
                                        !isBlocked(newX + dx, newY + i, newZ + j) -> openCurrent()
                                dy != 0 &&
                                        isBlocked(newX + i, newY, newZ + j) &&
                                        !isBlocked(newX + i, newY + dy, newZ + j) -> openCurrent()
                                dz != 0 &&
                                        isBlocked(newX + i, newY + j, newZ) &&
                                        !isBlocked(newX + i, newY + j, newZ + dz) -> openCurrent()
                                else -> continue
                            }
                        }
                    }
                }
                2 -> {

                    when {
                        dx == 0 -> {

                        }
                        dy == 0 -> {

                        }
                        dz == 0 -> {

                        }
                    }

                    // (2)
                    when {
                        dx != 0 && isBlocked(newX, y, z) && !isBlocked(newX + dx, y, z) -> return openCurrent()
                        dy != 0 && isBlocked(x, newY, z) && !isBlocked(x, newY + dy, z) -> return openCurrent()
                        dz != 0 && isBlocked(x, y, newZ) && !isBlocked(x, y, newZ + dz) -> return openCurrent()
                    }

                    // (3)

                    when {
                        dx != 0 && expand(newX, newY, newZ, dx, 0, 0) != null -> return openCurrent()
                        dy != 0 && expand(newX, newY, newZ, 0, dy, 0) != null -> return openCurrent()
                        dz != 0 && expand(newX, newY, newZ, 0, 0, dz) != null -> return openCurrent()
                    }
                }
                3 -> {

                    // (2)

                    if (isBlocked(x, newY, newZ) ||
                        isBlocked(newX, y, newZ) ||
                        isBlocked(newX, newY, z)
                    ) return openCurrent()

                    // (3)
                    when {
                        expand(newX, newY, newZ, dx, 0, 0) != null -> return openCurrent()
                        expand(newX, newY, newZ, 0, dy, 0) != null -> return openCurrent()
                        expand(newX, newY, newZ, 0, 0, dz) != null -> return openCurrent()
                        expand(newX, newY, newZ, dx, 0, dz) != null -> return openCurrent()
                        expand(newX, newY, newZ, 0, dy, dz) != null -> return openCurrent()
                        expand(newX, newY, newZ, dx, dy, 0) != null -> return openCurrent()
                    }
                }
            }

            x = newX
            y = newY
            z = newZ
        }
    }

    data class Node(val x: Int, val y: Int, val z: Int) {
        var closed = false
        var parent: Node? = null

        var g = Double.MAX_VALUE
        var h = -1.0
        val f
            get() = g + h

        fun distTo(other: Node) = distTo(other.x, other.y, other.z)

        fun distTo(x: Int, y: Int, z: Int) =
            sqrt((x.toDouble() - this.x).pow(2) + (y.toDouble() - this.y).pow(2) + (z.toDouble() - this.z).pow(2))
    }

}

typealias Coord = Triple<Int, Int, Int>