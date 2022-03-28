package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.InventorySlotInfo.Companion.getSlotsFromInventory
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

class LabEscape : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val scanTimer = TickTimer(7)
    val keys = arrayListOf<String>()

    override fun isEnabled() = PartyGamesHelper.labEscape

    override fun registerEventListeners() {
        on<SpecialTickEvent>().timerFilter(scanTimer).addSubscribe {
            val current = BlockPos(mc.thePlayer)
            val inventorySlotInfos = mc.thePlayer.getSlotsFromInventory()

            val (shovel, pickaxe, axe) = inventorySlotInfos.take(3).map { it.keyName }

            keys.clear()
            for (i in 1..5) {
                val key = when (mc.theWorld.getBlockAtPos(current.down(i))) {
                    Blocks.dirt -> shovel
                    Blocks.stone -> pickaxe
                    Blocks.planks -> axe
                    else -> continue
                }
                keys.add(key)
            }
        }
    }

    companion object : PartyMiniGamesCreator {
        override fun createImpl() = LabEscape()

        override val scoreboardIdentifier = "Lab Escape"
    }
}