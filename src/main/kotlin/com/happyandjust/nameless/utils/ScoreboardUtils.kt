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

import com.google.common.collect.Iterables
import com.google.common.collect.Lists
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.stripControlCodes
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.ScoreObjective
import net.minecraft.scoreboard.ScorePlayerTeam
import java.util.stream.Collectors

object ScoreboardUtils {

    /**
     * Taken from Danker's Skyblock Mod under GPL-3.0 License
     *
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun cleanSB(s: String): String {
        return StringBuilder().apply {
            for (c in s.stripControlCodes()) {
                append(if (c.code in 21..126) c else continue)
            }
        }.toString()
    }

    /**
     * Taken from Danker's Skyblock Mod under GPL-3.0 License
     *
     * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
     * @author bowser0000
     */
    fun getSidebarLines(cleanSB: Boolean = false): List<String> {
        val lines = arrayListOf<String>()

        val mc = Minecraft.getMinecraft()
        if (mc.theWorld == null) return lines

        val scoreboard = mc.theWorld.scoreboard ?: return lines

        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return lines

        var scores = scoreboard.getSortedScores(objective)

        val list = scores.stream().filter {
            it != null && it.playerName != null && !it.playerName.startsWith("#")
        }.collect(Collectors.toList())

        scores = if (list.size > 15) Lists.newArrayList(Iterables.skip(list, scores.size - 15)) else list

        for (score in scores) {
            val team = scoreboard.getPlayersTeam(score.playerName) ?: continue

            val s = ScorePlayerTeam.formatPlayerName(team, score.playerName)

            lines.add(if (cleanSB) cleanSB(s) else s)
        }

        return lines
    }

    fun getScoreboardDisplayName(): String {
        val scoreboard = mc.theWorld?.scoreboard ?: return ""

        var objective: ScoreObjective? = null
        val scorePlayersTeam = scoreboard.getPlayersTeam(mc.thePlayer.name)
        scorePlayersTeam?.let {
            objective = scoreboard.getObjectiveInDisplaySlot((it.chatFormat.colorIndex.takeIf { slot -> slot >= 0 }
                ?: return@let) + 3)
        }

        return (objective ?: scoreboard.getObjectiveInDisplaySlot(1))?.displayName ?: ""
    }
}
