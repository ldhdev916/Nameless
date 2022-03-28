package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.dsl.drawFilledBox
import com.happyandjust.nameless.dsl.getAxisAlignedBB
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent

class AnvilSpleef : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val renderingCallbacks = hashSetOf<(Float) -> Unit>()

    override fun isEnabled() = PartyGamesHelper.anvil

    override fun registerEventListeners() {
        on<SpecialTickEvent>().addSubscribe {
            renderingCallbacks.clear()
            val anvils = getAnvils()
            anvils.forEach {
                val aabb = BlockPos(it.posX, 0.0, it.posZ).getAxisAlignedBB()
                renderingCallbacks.add { partialTicks ->
                    aabb.drawFilledBox(anvilColor, partialTicks)
                }
            }
        }

        on<RenderWorldLastEvent>().addSubscribe {
            renderingCallbacks.forEach { it(partialTicks) }
        }
    }

    private fun getAnvils() = mc.theWorld.loadedEntityList
        .filterIsInstance<EntityFallingBlock>()
        .filter { it.posY > 1 && it.block.block == Blocks.anvil }

    companion object : PartyMiniGamesCreator {
        override fun createImpl() = AnvilSpleef()

        override val scoreboardIdentifier = "Anvil Spleef"

        private val anvilColor
            get() = PartyGamesHelper.anvilColor.rgb
    }
}