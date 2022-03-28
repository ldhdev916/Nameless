package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper
import net.minecraft.entity.player.EntityPlayer

class RPG16 : PartyMiniGames {

    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = PartyGamesHelper.rpg16

    override fun registerEventListeners() {
        on<OutlineRenderEvent>().filter { entity is EntityPlayer && entity.health <= 2.1f }.addSubscribe {
            colorInfo = ColorInfo(outlineColor, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : PartyMiniGamesCreator {

        private val outlineColor
            get() = PartyGamesHelper.rpg16Color.rgb

        override fun createImpl() = RPG16()

        override val scoreboardIdentifier = "RPG-16"
    }
}