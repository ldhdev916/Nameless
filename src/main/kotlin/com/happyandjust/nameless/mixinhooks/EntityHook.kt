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

import com.happyandjust.nameless.dsl.instanceOrNull
import com.happyandjust.nameless.dsl.matchesMatcher
import com.happyandjust.nameless.dsl.stripControlCodes
import com.happyandjust.nameless.dsl.transformToPrecisionString
import com.happyandjust.nameless.features.impl.skyblock.FeatureDamageIndicator
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.DamageIndicateType
import net.minecraft.util.ChatComponentText
import java.util.regex.Pattern

object EntityHook {

    private val CRIT_DAMAGE = Pattern.compile("✧(?<damage>\\d+)✧")
    private val DAMAGE_REGEX = "\\d+".toRegex()
    val transformedDamageCache = hashMapOf<String, ChatComponentText>()
    private val critDamageColor = arrayOf("§f", "§f", "§e", "§6", "§c", "§c")
    private val smartTransform = { damage: Int ->
        when (damage) {
            in 0 until 1000 -> damage.toDouble() to ""
            in 1000 until 100_0000 -> damage / 1000.0 to "K"
            in 100_0000 until 10_0000_0000 -> damage / 100_0000.0 to "M"
            else -> damage / 10_0000_0000.0 to "B"
        }
    }

    private fun transformDamage(damage: Int, type: DamageIndicateType, precision: Int): String {

        val name = when (type) {
            DamageIndicateType.SMART -> smartTransform(damage).second
            else -> type.name
        }

        return when (type) {
            DamageIndicateType.K -> (damage / 1000.0)
            DamageIndicateType.M -> (damage / 100_0000.0)
            DamageIndicateType.B -> (damage / 10_0000_0000.0)
            DamageIndicateType.SMART -> smartTransform(damage).first
        }.transformToPrecisionString(precision) + name
    }

    fun getCustomDamageName(origin: ChatComponentText): ChatComponentText {

        if (Hypixel.currentGame == GameType.SKYBLOCK && FeatureDamageIndicator.enabled) {

            val unformattedText = origin.unformattedText

            transformedDamageCache.getOrPut(unformattedText) {
                val (damage, isCritical) = getDamageFromString(unformattedText.stripControlCodes()) ?: return origin

                val damageText =
                    transformDamage(damage, FeatureDamageIndicator.type, FeatureDamageIndicator.precision)

                ChatComponentText(
                    if (isCritical) { // critical
                        "✧$damageText✧".withIndex().joinToString("") { (index, char) ->
                            "${critDamageColor[index % 6]}$char"
                        }
                    } else {
                        "§${origin.formattedText[1]}$damageText"
                    }
                )
            }
        }

        return origin
    }

    private fun getDamageFromString(text: String): Pair<Int, Boolean>? {
        if (text.matches(DAMAGE_REGEX)) return instanceOrNull(text.toIntOrNull(), false, ::Pair)
        return CRIT_DAMAGE.matchesMatcher(text) {
            instanceOrNull(group("damage").toIntOrNull(), true, ::Pair)
        }
    }
}