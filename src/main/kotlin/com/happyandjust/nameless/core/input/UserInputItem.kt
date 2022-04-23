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

package com.happyandjust.nameless.core.input

import net.minecraft.util.EnumChatFormatting

const val COLOR_INPUT_CHAR = '∮'

const val VALUE_OPEN_BRACKET = '《'
const val VALUE_CLOSE_BRACKET = '》'

@kotlinx.serialization.Serializable
sealed interface UserInputItem {
    fun asString(valueMap: Map<String, Any?>): String

    fun asString(vararg valuePairs: Pair<String, Any?>) = asString(valuePairs.toMap())

    fun asPreviewString(): String

    companion object {
        fun parseFromPreviewString(s: String): UserInputItem {
            val items = mutableListOf<UserInputItem>()
            var buffer = ""

            val clearBuffer = {
                if (buffer.isNotEmpty()) {
                    items.add(TextInputItem(buffer))
                    buffer = ""
                }
            }

            var skipNum = 0

            for ((index, char) in s.withIndex()) {
                if (skipNum > 0) {
                    skipNum--
                    continue
                }
                when (char) {
                    COLOR_INPUT_CHAR -> {
                        val colorCode = "§${s.getOrNull(index + 1)}"
                        val enumChatFormatting = EnumChatFormatting.values().singleOrNull { it.toString() == colorCode }
                        if (enumChatFormatting != null) {
                            clearBuffer()
                            items.add(ColorInputItem(enumChatFormatting))

                            skipNum = 1
                        } else {
                            buffer += char
                        }
                    }
                    VALUE_OPEN_BRACKET -> {
                        val close = s.indexOf(VALUE_CLOSE_BRACKET, startIndex = index)
                        if (close != -1) {
                            val valuePlaceHolder = s.substring(index + 1, close)
                            if (valuePlaceHolder.all { it.isLetterOrDigit() }) {
                                skipNum = close - index

                                clearBuffer()
                                items.add(ValueInputItem(valuePlaceHolder))
                                continue
                            }
                        }

                        buffer += char

                    }
                    else -> buffer += char
                }
            }

            if (items.isEmpty()) {
                return TextInputItem(buffer)
            }
            if (buffer.isNotEmpty()) items.add(TextInputItem(buffer))

            return items.singleOrNull() ?: CompositeInputItem(items)
        }
    }
}

@kotlinx.serialization.Serializable
data class TextInputItem(private val text: String) : UserInputItem {

    override fun asString(valueMap: Map<String, Any?>) = text

    override fun asPreviewString() = text
}

@kotlinx.serialization.Serializable
data class ColorInputItem(private val color: EnumChatFormatting) : UserInputItem {

    override fun asString(valueMap: Map<String, Any?>) = color.toString()

    override fun asPreviewString() = asString().replace('§', COLOR_INPUT_CHAR)
}

@kotlinx.serialization.Serializable
data class ValueInputItem(private val placeholder: String) : UserInputItem {

    override fun asString(valueMap: Map<String, Any?>) =
        valueMap[placeholder]?.toString() ?: " §c[WRONG_PLACE_HOLDER: $placeholder]§r "

    override fun asPreviewString() = VALUE_OPEN_BRACKET + placeholder + VALUE_CLOSE_BRACKET
}

@kotlinx.serialization.Serializable
data class CompositeInputItem(private val items: List<UserInputItem>) : UserInputItem {

    override fun asString(valueMap: Map<String, Any?>) = `as` { it.asString(valueMap) }

    override fun asPreviewString() = `as` { it.asPreviewString() }

    private fun `as`(transform: (UserInputItem) -> String) = items.joinToString("", transform = transform)
}