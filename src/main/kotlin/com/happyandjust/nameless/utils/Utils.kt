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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.InventorySlotInfo
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import net.minecraft.block.material.Material
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.BlockPos
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.io.File
import java.io.IOException

object Utils {

    fun isKeyDown(keyCode: Int): Boolean {

        if (keyCode == Keyboard.KEY_NONE) return false

        return if (keyCode < 0) Mouse.isButtonDown(keyCode + 100) else Keyboard.isKeyDown(keyCode)
    }

    fun getKeyBindingNameInEverySlot(): Map<Int, InventorySlotInfo> {
        val inventory = mc.thePlayer.inventory

        return mc.gameSettings.keyBindsHotbar
            .associate {
                val slot = it.keyDescription.last().digitToInt() - 1

                slot to InventorySlotInfo(slot, Keyboard.getKeyName(it.keyCode), inventory.getStackInSlot(slot))
            }
    }

    fun getHighestGround(pos: BlockPos, shouldBeValid: Boolean): BlockPos {
        var pos = pos
        val world = mc.theWorld
        while (world.getBlockAtPos(pos).let { it.isPassable(world, pos) && it.material != Material.water }) {
            pos = pos.add(0, -1, 0)
            if (pos.y <= 0) return pos
        }

        return if (shouldBeValid) pos.up() else pos
    }

    fun getPlayersInTab(): List<EntityPlayer> {
        return mc.netHandler.playerInfoMap
            .filter { it.gameProfile.name != null }
            .mapNotNull { mc.theWorld.getPlayerEntityByName(it.gameProfile.name) }
    }

    /**
     * @link https://stackoverflow.com/a/47925649
     */
    @Throws(IOException::class)
    fun getJavaRuntime(): String {
        val os = System.getProperty("os.name")
        val java = "${System.getProperty("java.home")}${File.separator}bin${File.separator}${
            if (os != null && os.lowercase().startsWith("windows")) "java.exe" else "java"
        }"
        if (!File(java).isFile) {
            throw IOException("Unable to find suitable java runtime at $java")
        }
        return java
    }

    /**
     * Taken from Skytils under AGPL-3.0
     *
     * Modified
     *
     * https://github.com/Skytils/SkytilsMod/blob/1.x/LICENSE.md
     */
    fun deleteOldJar() {
        val modFile = Nameless.modFile

        if (modFile.delete()) {
            return
        }
        val runtime = getJavaRuntime()

        val file = File("config/HappyAndJust/Deleter.jar")

        val cmd = "\"$runtime\" -jar \"${file.absolutePath}\" \"${modFile.absolutePath}\""

        Runtime.getRuntime().exec(cmd)

    }


}