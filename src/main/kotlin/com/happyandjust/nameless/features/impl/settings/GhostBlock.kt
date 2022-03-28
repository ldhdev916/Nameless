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

package com.happyandjust.nameless.features.impl.settings

import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.KeyPressEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.ComponentType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.SkyBlock
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockLever
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition

object GhostBlock : SimpleFeature(
    "ghostBlock",
    "Ghost Block",
    "Make client-side air where you are looking at"
) {

    init {
        hierarchy {
            +::restore

            +::ignoreSecret
        }
    }

    private val ghostBlocks = hashMapOf<BlockPos, BlockInfo>()

    private var restore by parameter(10) {
        key = "restore"
        title = "Restore Seconds"
        desc =
            "after the seconds you selected, block you made client-side air will be restored back\n-1 for not restoring back"

        settings {
            minValueInt = -1
            maxValueInt = 60
        }
    }

    private var ignoreSecret by parameter(true) {
        key = "ignoreSecret"
        title = "Ignore Dungeons Secrets"
        desc = "Prevent making skyblock dungeons secrets ghost-block"

        settings {
            ordinal = 1
        }
    }

    override var componentType: ComponentType? = null

    init {
        on<KeyPressEvent>().filter { !inGui && keyBindingCategory == KeyBindingCategory.GHOST_BLOCK }.subscribe {
            mc.objectMouseOver?.takeIf { it.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK }?.let {
                makeGhostBlock(it.blockPos)
            }
        }

        on<SpecialTickEvent>().filter { ghostBlocks.isNotEmpty() }.subscribe {

            val iterator = ghostBlocks.iterator()

            for ((pos, blockInfo) in iterator) {
                if (mc.theWorld.getBlockAtPos(pos) != Blocks.air) { // did the pos state changed by server?
                    iterator.remove()
                    continue
                }
                if (blockInfo.tick == 0) { // restore
                    mc.theWorld.setBlockState(pos, blockInfo.blockState)
                    iterator.remove()
                }

                blockInfo.tick--
            }
        }
    }

    private fun makeGhostBlock(pos: BlockPos) {
        val currentGame = Hypixel.currentGame
        if (currentGame is SkyBlock && currentGame.inDungeon && ignoreSecret) {
            when (mc.theWorld.getBlockAtPos(pos)) {
                is BlockChest, is BlockLever, is BlockSkull -> return
            }
        }

        ghostBlocks[pos] = BlockInfo(mc.theWorld.getBlockState(pos))
        mc.theWorld.setBlockState(pos, Blocks.air.defaultState)
    }

    class BlockInfo(var blockState: IBlockState) {
        var tick = restore * 20
    }

    init {
        on<PacketEvent.Received>().filter { ghostBlocks.isNotEmpty() }.subscribe {

            when (val msg = packet) {
                is S22PacketMultiBlockChange -> {
                    val data =
                        msg.changedBlocks.find { it.pos in ghostBlocks && it.blockState != null } ?: return@subscribe
                    ghostBlocks[data.pos]?.blockState = data.blockState

                }
                is S23PacketBlockChange -> {
                    ghostBlocks[msg.blockPosition]?.blockState = msg.blockState ?: return@subscribe
                }
            }
        }
    }
}