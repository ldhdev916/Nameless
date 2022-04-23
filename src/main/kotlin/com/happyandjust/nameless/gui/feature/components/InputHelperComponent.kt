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

package com.happyandjust.nameless.gui.feature.components

import com.happyandjust.nameless.core.input.COLOR_INPUT_CHAR
import com.happyandjust.nameless.core.input.InputPlaceHolder
import com.happyandjust.nameless.core.input.VALUE_CLOSE_BRACKET
import com.happyandjust.nameless.core.input.VALUE_OPEN_BRACKET
import com.happyandjust.nameless.dsl.mc
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.invisible
import gg.essential.elementa.utils.withAlpha
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.EnumChatFormatting
import java.awt.Color

interface InputHelperComponent {
    fun createComponent(insertText: (String) -> Unit): UIComponent
}

object ColorHelperComponent : InputHelperComponent {

    private fun EnumChatFormatting.createComponent(insertText: (String) -> Unit): UIComponent {
        val colorCode = toString()[1]

        return runCatching {
            val awtColor = Color(mc.fontRendererObj.getColorCode(colorCode))
            UIBlock(awtColor).onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, awtColor.darker().constraint)
                }
            }.onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, awtColor.constraint)
                }
            }
        }.getOrElse {

            val text = when (this) {
                EnumChatFormatting.OBFUSCATED -> "§kO"
                EnumChatFormatting.BOLD -> "§lB"
                EnumChatFormatting.STRIKETHROUGH -> "§mV"
                EnumChatFormatting.UNDERLINE -> "§nU"
                EnumChatFormatting.ITALIC -> "§oI"
                else -> "§cR"
            }

            val container = UIBlock(Color.white.invisible()).onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, Color.lightGray.withAlpha(0.2f).constraint)
                }
            }.onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, Color.white.invisible().constraint)
                }
            }


            UIText(text).constrain {
                x = CenterConstraint()
                y = CenterConstraint()

                textScale = 1.6.pixels()
            } childOf container

            container
        }.constrain {
            width = 16.pixels()
            height = AspectConstraint()
        }.onLeftClick {
            insertText("${COLOR_INPUT_CHAR}$colorCode")
        }
    }

    override fun createComponent(insertText: (String) -> Unit): UIComponent {
        val container = UIContainer().constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        for (colors in EnumChatFormatting.values().toList().chunked(6)) {
            val colorContainer = UIContainer().constrain {

                y = SiblingConstraint(6f)

                width = ChildBasedSizeConstraint()
                height = ChildBasedMaxSizeConstraint()
            } childOf container

            colors.forEach {
                it.createComponent(insertText).constrain {
                    x = SiblingConstraint(4f)
                } childOf colorContainer
            }
        }

        return container
    }
}

class ValueHelperComponent(private val placeHolders: Iterable<InputPlaceHolder>) : InputHelperComponent {
    override fun createComponent(insertText: (String) -> Unit): UIComponent {
        val holder = UIContainer().constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        UIText("Values").constrain {
            textScale = 2.pixels()
        } childOf holder

        placeHolders.forEach { (name, desc) ->
            val rowHolder = UIContainer().constrain {

                y = SiblingConstraint(2f)

                width = ChildBasedSizeConstraint()
                height = ChildBasedMaxSizeConstraint()
            }

            val nameHolder = UIBlock(Color.white.invisible()).constrain {
                width = ChildBasedSizeConstraint()
                height = ChildBasedSizeConstraint()
            }.onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, Color.gray.withAlpha(.2f).constraint)
                }
            }.onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, Color.white.invisible().constraint)
                }
            }.onLeftClick {
                insertText("${VALUE_OPEN_BRACKET}$name${VALUE_CLOSE_BRACKET}")
            } childOf rowHolder

            val textScalePixels = 1.5.pixels()

            UIText(name).constrain {
                textScale = textScalePixels
            } childOf nameHolder

            UIText(" : ").constrain {
                x = SiblingConstraint()
                textScale = textScalePixels
            } childOf rowHolder

            UIWrappedText(desc).constrain {
                width = 100.pixels()
                textScale = textScalePixels
            }.constrain {
                x = SiblingConstraint()
                y = CenterConstraint()
            } childOf rowHolder

            rowHolder childOf holder
        }

        return holder
    }
}