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

import com.happyandjust.nameless.devqol.formatDouble
import com.happyandjust.nameless.devqol.matchesMatcher
import com.happyandjust.nameless.devqol.stripControlCodes
import com.happyandjust.nameless.devqol.transformToPrecision
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import net.minecraft.util.ChatComponentText
import java.util.regex.Pattern

object EntityHook {

    private val CRIT_DAMAGE = Pattern.compile("✧(?<damage>\\d+)✧")
    private val DAMAGE_REGEX = "\\d+".toRegex()
    private val transformedDamageCache = hashMapOf<String, ChatComponentText>()
    private val critDamageColor = arrayOf("§f", "§f", "§e", "§6", "§c", "§c")
    private var prevIndicateType: DamageIndicateType? = null
    private var prevPrecision: Int? = null

    private fun transformDamage(damage: Int, type: DamageIndicateType, precision: Int): String {
        return when (type) {
            DamageIndicateType.K -> (damage / 1000.0).transformToPrecision(precision)
            DamageIndicateType.M -> (damage / 100_0000.0).transformToPrecision(precision)
            DamageIndicateType.B -> (damage / 10_0000_0000.0).transformToPrecision(precision)
        }.formatDouble() + type.name
    }

    fun getCustomDamageName(origin: ChatComponentText): ChatComponentText {

        if (Hypixel.currentGame == GameType.SKYBLOCK && FeatureRegistry.DAMAGE_INDICATOR.enabled) {

            val unformattedText = origin.unformattedText.replace(",", "") // neu

            //
            val type = FeatureRegistry.DAMAGE_INDICATOR.getParameterValue<DamageIndicateType>("type")

            if (prevIndicateType != type) {
                transformedDamageCache.clear()
            }

            prevIndicateType = type

            val precision = FeatureRegistry.DAMAGE_INDICATOR.getParameterValue<Int>("precision")

            if (prevPrecision != precision) {
                transformedDamageCache.clear()
            }

            prevPrecision = precision
            //

            if (transformedDamageCache.containsKey(unformattedText)) return transformedDamageCache[unformattedText]!!

            val damagePair = getDamageFromString(unformattedText.stripControlCodes()) ?: return origin

            val damageText = transformDamage(damagePair.first, type, precision)

            val chatComponentText = if (damagePair.second) { // critical
                val builder = StringBuilder()

                for ((index, char) in "✧$damageText✧".withIndex()) {
                    builder.append(critDamageColor[index % 6]).append(char)
                }

                ChatComponentText(builder.toString())
            } else {
                ChatComponentText("§7$damageText")
            }

            transformedDamageCache[unformattedText] = chatComponentText

            return chatComponentText
        }

        return origin
    }

    private fun getDamageFromString(text: String): Pair<Int, Boolean>? {
        if (text.matches(DAMAGE_REGEX)) return text.toInt() to false

        var damage: Pair<Int, Boolean>? = null

        CRIT_DAMAGE.matchesMatcher(text) {
            damage = it.group("damage").toInt() to true
        }

        return damage
    }
}