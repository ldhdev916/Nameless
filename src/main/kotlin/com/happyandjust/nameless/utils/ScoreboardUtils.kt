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

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.stripControlCodes
import net.minecraft.scoreboard.ScorePlayerTeam

object ScoreboardUtils {

    /**
     * Taken from Danker's Skyblock Mod under GPL-3.0 License
     *
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun cleanSB(s: String) = s.stripControlCodes().filter { it.code in 21..126 }

    /**
     * Taken from Danker's Skyblock Mod under GPL-3.0 License
     *
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun getSidebarLines(cleanSB: Boolean = false): List<String> {
        val lines = arrayListOf<String>()

        val scoreboard = mc.theWorld?.scoreboard ?: return lines

        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return lines

        var scores = scoreboard.getSortedScores(objective)

        val list = scores.filter { it?.playerName?.startsWith("#") == false }

        scores = if (list.size > 15) list.drop(list.size - 15) else list

        for (score in scores) {
            val team = scoreboard.getPlayersTeam(score.playerName) ?: continue

            lines.add(ScorePlayerTeam.formatPlayerName(team, score.playerName))
        }

        return if (cleanSB) lines.map { cleanSB(it) } else lines
    }
}