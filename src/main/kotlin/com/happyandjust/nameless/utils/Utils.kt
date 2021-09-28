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

package com.happyandjust.nameless.utils

import com.happyandjust.nameless.core.InventorySlotInfo
import com.happyandjust.nameless.devqol.getBlockAtPos
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.mixins.accessors.AccessorGuiPlayerTabOverlay
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object Utils {

    fun isKeyDown(keyCode: Int): Boolean {

        if (keyCode == Keyboard.KEY_NONE) return false

        return if (keyCode < 0) Mouse.isButtonDown(keyCode + 100) else Keyboard.isKeyDown(keyCode)
    }

    fun getKeyBindingNameInEverySlot(): Map<Int, InventorySlotInfo> {

        val map = hashMapOf<Int, InventorySlotInfo>()

        val inventory = mc.thePlayer.inventory

        for (keyBinding in mc.gameSettings.keyBindsHotbar) {
            val slot = keyBinding.keyDescription.split("key.hotbar.")[1].toInt() - 1
            map[slot] = InventorySlotInfo(slot, Keyboard.getKeyName(keyBinding.keyCode), inventory.getStackInSlot(slot))
        }

        return map
    }

    fun getHighestGround(pos: BlockPos, shouldBeValid: Boolean): BlockPos {
        var pos = pos
        val world = mc.theWorld
        while (world.getBlockAtPos(pos).isPassable(world, pos)) {
            pos = pos.add(0, -1, 0)
            if (pos.y <= 0) return pos
        }

        return if (shouldBeValid) pos.up() else pos
    }

    fun getSeconds(start: Long, end: Long) = (end - start) / 10_0000_0000.0

    fun getPlayersInTab(): List<EntityPlayer> {
        val players = arrayListOf<EntityPlayer>()

        for (info in AccessorGuiPlayerTabOverlay.field_175252_a().sortedCopy(mc.netHandler.playerInfoMap)) {
            val name = info.gameProfile.name ?: continue

            players.add(mc.theWorld.getPlayerEntityByName(name) ?: continue)
        }

        return players
    }
}
