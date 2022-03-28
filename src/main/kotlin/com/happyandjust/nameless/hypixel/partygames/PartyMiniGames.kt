package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.dsl.TempEventListener

interface PartyMiniGames : TempEventListener {
    fun isEnabled(): Boolean
}

interface PartyMiniGamesCreator {
    fun createImpl(): PartyMiniGames

    val scoreboardIdentifier: String
}