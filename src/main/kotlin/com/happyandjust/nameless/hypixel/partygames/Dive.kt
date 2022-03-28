package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.dsl.drawFilledBox
import com.happyandjust.nameless.dsl.getBlockAtPos
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

class Dive : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private var expectedAABB: AxisAlignedBB? = null
    private var isCollide = false

    override fun isEnabled() = PartyGamesHelper.dive

    override fun registerEventListeners() {
        on<SpecialTickEvent>().addSubscribe {
            val playerAABB = mc.thePlayer.entityBoundingBox

            val fallAABB = with(playerAABB) {
                AxisAlignedBB(minX, 14.0, minZ, maxX, 15.0, maxZ)
            }
            expectedAABB = fallAABB
            for (x in arrayOf(fallAABB.minX, fallAABB.maxX)) {
                for (z in arrayOf(fallAABB.minZ, fallAABB.maxZ)) {
                    val pos = BlockPos(x, fallAABB.minY, z)
                    if (mc.theWorld.getBlockAtPos(pos) != Blocks.water) {
                        isCollide = true
                        return@addSubscribe
                    }
                }
            }
            isCollide = false
        }

        on<RenderWorldLastEvent>().addSubscribe {
            expectedAABB?.drawFilledBox(if (isCollide) Color.red.rgb else boxColor, partialTicks)
        }
    }


    companion object : PartyMiniGamesCreator {

        private val boxColor
            get() = PartyGamesHelper.diveColor.rgb

        override fun createImpl() = Dive()

        override val scoreboardIdentifier = "Dive"
    }

}