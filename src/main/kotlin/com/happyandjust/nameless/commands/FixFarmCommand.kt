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

package com.happyandjust.nameless.commands

import com.happyandjust.nameless.core.ClientCommandBase
import com.happyandjust.nameless.core.FarmFixData
import com.happyandjust.nameless.devqol.getBlockAtPos
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.sendClientMessage
import com.happyandjust.nameless.devqol.toBlockPos
import com.happyandjust.nameless.mixins.accessors.AccessorBlockFarmland
import net.minecraft.block.BlockFarmland
import net.minecraft.block.BlockNetherWart
import net.minecraft.block.BlockSoulSand
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos

object FixFarmCommand : ClientCommandBase("fixfarm") {

    val problemBlocks = arrayListOf<FarmFixData>()

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {

        if (args.size == 1 && args[0] == "clear") {
            problemBlocks.clear()
            sendClientMessage("§aClear!")
            return
        }

        if (args.size != 6) {
            sendClientMessage("§cUsage: /fixfarm [x1] [y1] [z1] [x2] [y2] [z2] or /fixfarm clear")
            return
        }

        problemBlocks.clear()

        val pos1 = args.toBlockPos(0..2)
        val pos2 = args.toBlockPos(3..5)

        val world = mc.theWorld

        for (pos in BlockPos.getAllInBox(pos1, pos2)) {
            val blockState = world.getBlockState(pos)

            when (val block = blockState.block) {
                is BlockSoulSand -> {
                    if (world.getBlockAtPos(pos.up()) !is BlockNetherWart) {
                        problemBlocks.add(
                            FarmFixData(
                                pos,
                                "No NetherWart"
                            ) { world.getBlockAtPos(pos.up()) is BlockNetherWart })
                    }
                }
                is BlockFarmland -> {
                    if (!(block as AccessorBlockFarmland).invokeHasCrops(world, pos)) {
                        problemBlocks.add(
                            FarmFixData(
                                pos,
                                "No Crops",
                            ) { (block as AccessorBlockFarmland).invokeHasCrops(world, pos) }
                        )
                    }
                }
            }
        }
    }
}