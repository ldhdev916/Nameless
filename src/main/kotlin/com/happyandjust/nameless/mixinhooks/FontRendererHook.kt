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

package com.happyandjust.nameless.mixinhooks

import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.impl.misc.FeatureDisguiseNickname
import java.util.regex.Matcher
import java.util.regex.Pattern

object FontRendererHook {

    val cache = hashMapOf<String, List<MatchInfo>>()
    private var prevSessionName: String? = null

    fun isMatching(i: Int, matchInfos: List<MatchInfo>): Boolean {
        for (matchInfo in matchInfos) {
            if (matchInfo.isBetween(i)) return true
        }

        return false
    }

    fun getMatchInfos(text: String?): List<MatchInfo> {
        var text = text ?: return emptyList()
        val nickname = getCurrentSessionNickname()

        if (nickname.isBlank()) return emptyList()

        val list = arrayListOf(MatchInfo(-1, -1, 0))

        val session = getCurrentSessionNickname()

        if (session != prevSessionName) {
            cache.clear()
        }

        prevSessionName = session

        val pattern = Pattern.compile("(?i)$session")
        var matcher: Matcher = pattern.matcher(text)
        while (matcher.find()) {
            val addNext = list.last().addNext
            list.add(MatchInfo(matcher.start() + addNext, matcher.end() + addNext, addNext + nickname.length))
            text = matcher.replaceFirst("")
            matcher = pattern.matcher(text)
        }

        list.removeAt(0)
        return list
    }

    private fun getCurrentSessionNickname(): String {
        return if (FeatureDisguiseNickname.enabled) FeatureDisguiseNickname.nick else mc.session.username ?: ""
    }


    data class MatchInfo(val start: Int, val end: Int, val addNext: Int) {
        fun isBetween(index: Int) = index in start until end
    }
}