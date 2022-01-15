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

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.matrix
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.setup
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import com.happyandjust.nameless.processor.Processor
import com.happyandjust.nameless.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import java.awt.Color

object LabEscapeProcessor : Processor() {

    lateinit var overlay: () -> Overlay
    private val keys = arrayListOf<String>()
    private val timer = TickTimer(7)
    override val filter = PartyGamesHelper.getFilter(this)

    init {
        request<SpecialTickEvent>().filter { timer.update().check() }.subscribe {
            keys.clear()

            val world = mc.theWorld
            val base = BlockPos(mc.thePlayer)

            val keyBindingMap = Utils.getKeyBindingNameInEverySlot()

            val shovel = keyBindingMap[0]!!.keyName
            val pickaxe = keyBindingMap[1]!!.keyName
            val axe = keyBindingMap[2]!!.keyName

            for (i in 1..5) {
                keys.add(
                    when (world.getBlockAtPos(base.down(i))) {
                        Blocks.dirt -> shovel
                        Blocks.stone -> pickaxe
                        Blocks.planks -> axe
                        else -> continue
                    }
                )
            }
        }
        request<SpecialOverlayEvent>().subscribe {
            matrix {
                setup(overlay())
                mc.fontRendererObj.drawSplitString(keys.joinToString("\n"), 0, 0, Int.MAX_VALUE, Color.red.rgb)
            }
        }
    }
}