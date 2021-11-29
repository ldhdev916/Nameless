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

package com.happyandjust.nameless.features.impl.settings

import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SettingFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.PacketListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CInt
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockLever
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition

object FeatureGhostBlock : SettingFeature(
    "ghostblock",
    "Ghost Block",
    "Make client-side air where you are looking at"
), ClientTickListener, PacketListener {

    private val ghostBlocks = hashMapOf<BlockPos, BlockInfo>()

    init {
        parameters["restore"] = FeatureParameter(
            0,
            "ghostblock",
            "restore",
            "Restore Seconds",
            "after the seconds you selected, block you made client-side air will be restored back\n-1 for not restoring back",
            10,
            CInt
        ).apply {
            minValue = -1.0
            maxValue = 60.0
        }
        parameters["ignore"] = FeatureParameter(
            1,
            "ghostblock",
            "ignoresecret",
            "Ignore Dungeons Secrets",
            "Prevent making skyblock dungeons secrets ghost-block",
            true,
            CBoolean
        )
    }

    override fun tick() {
        if (mc.inGameHasFocus && KeyBindingCategory.GHOST_BLOCK.getKeyBinding().isKeyDown) {
            mc.objectMouseOver?.takeIf { it.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK }?.let {
                makeGhostBlock(it.blockPos)
            }
        }

        if (ghostBlocks.isEmpty()) return

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

    private fun makeGhostBlock(pos: BlockPos) {
        if (Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty(PropertyKey.DUNGEON) && getParameterValue("ignore")) {
            when (mc.theWorld.getBlockAtPos(pos)) {
                is BlockChest, is BlockLever, is BlockSkull -> return
            }
        }

        ghostBlocks[pos] = BlockInfo(mc.theWorld.getBlockState(pos))
        mc.theWorld.setBlockState(pos, Blocks.air.defaultState)
    }

    class BlockInfo(var blockState: IBlockState) {
        var tick = getParameterValue<Int>("restore") * 20
    }

    override fun onSendingPacket(e: PacketEvent.Sending) {

    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
        if (ghostBlocks.isEmpty()) return

        when (val msg = e.packet) {
            is S22PacketMultiBlockChange -> {
                val data = msg.changedBlocks.find { it.pos in ghostBlocks && it.blockState != null } ?: return
                ghostBlocks[data.pos]?.blockState = data.blockState

            }
            is S23PacketBlockChange -> {
                ghostBlocks[msg.blockPosition]?.blockState = msg.blockState ?: return
            }
        }
    }
}