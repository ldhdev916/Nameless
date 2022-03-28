package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.impl.qol.PartyGamesHelper

class AnimalSlaughter : PartyMiniGames {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = PartyGamesHelper.animal

    override fun registerEventListeners() {
        on<OutlineRenderEvent>().filter { "-50%" in entity.displayName.unformattedText }.addSubscribe {
            colorInfo = ColorInfo(outlineColor, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : PartyMiniGamesCreator {
        override fun createImpl() = AnimalSlaughter()

        override val scoreboardIdentifier = "Animal Slaughter"

        private val outlineColor
            get() = PartyGamesHelper.animalColor.rgb
    }
}