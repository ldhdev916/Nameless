package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.getSidebarLines
import com.happyandjust.nameless.dsl.matchesMatcher
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.entity.player.EntityPlayer

class HighGround : PartyMiniGames {

    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    private val scanTimer = TickTimer.withSecond(0.5)
    private val higherPlayers = hashSetOf<EntityPlayer>()

    override fun isEnabled() = PartyGamesHelper.highGround

    override fun registerEventListeners() {
        on<SpecialTickEvent>().timerFilter(scanTimer).addSubscribe {
            val playersInScoreboard = buildList {
                for (line in mc.theWorld.getSidebarLines()) {
                    SCOREBOARD_PATTERN.matchesMatcher(line) {
                        val playerName = group("name")
                        val score = group("score").toInt()

                        add(playerName to score)
                    }
                }
            }.sortedByDescending { it.second }.map { it.first }

            higherPlayers.clear()
            higherPlayers.addAll(
                playersInScoreboard.takeWhile { it != mc.thePlayer.name }
                    .mapNotNull { mc.theWorld.getPlayerEntityByName(it) }
            )
        }

        on<OutlineRenderEvent>().filter { entity in higherPlayers }.addSubscribe {
            colorInfo = ColorInfo(outlineColor, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : PartyMiniGamesCreator {

        private val outlineColor
            get() = PartyGamesHelper.highGroundColor.rgb
        private val SCOREBOARD_PATTERN = "(?<name>\\w+): (?<score>\\d+)".toPattern()

        override fun createImpl() = HighGround()

        override val scoreboardIdentifier = "High Ground"
    }
}