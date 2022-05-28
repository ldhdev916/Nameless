/*
 * Nameless - 1.8.9 Hypixel Quality Of Life Mod
 * Copyright (C) 2022 HappyAndJust
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

package com.happyandjust.nameless.hypixel.partygames

import com.happyandjust.nameless.dsl.TempEventListener

interface PartyMiniGames : TempEventListener {
    fun isEnabled(): Boolean
}

fun interface PartyMiniGamesFactory {
    fun createPartyMiniGames(scoreboardLine: String): PartyMiniGames?
}

object PartyMiniGamesFactoryImpl : PartyMiniGamesFactory {
    override fun createPartyMiniGames(scoreboardLine: String) = when {
        scoreboardLine find "Animal Slaughter" -> AnimalSlaughter()
        scoreboardLine find "Anvil Spleef" -> AnvilSpleef()
        scoreboardLine find "Avalanche" -> Avalanche()
        scoreboardLine find "Dive" -> Dive()
        scoreboardLine find "High Ground" -> HighGround()
        scoreboardLine find "Jigsaw Rush" -> JigsawRush()
        scoreboardLine find "Lab Escape" -> LabEscape()
        scoreboardLine find "RPG-16" -> RPG16()
        scoreboardLine find "Spider Maze" -> SpiderMaze()
        else -> null
    }

    private infix fun String.find(other: String) = contains(other, true)
}