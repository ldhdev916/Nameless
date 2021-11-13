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

package com.happyandjust.nameless.gui.feature

import com.happyandjust.nameless.MOD_NAME
import com.happyandjust.nameless.VERSION
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.AspectConstraint
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.utils.invisible
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.util.ChatAllowedCharacters
import java.net.URL

class FeatureTitleBar(gui: FeatureGui, window: Window) : UIContainer() {

    private val searchEnabled
        get() = searchTextInput.hasFocus()

    init {

        enableEffect(ScissorEffect())

        UIBlock(ColorCache.lightBackground).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf this

        UIText("$MOD_NAME v$VERSION by HappyAndJust").constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            textScale = 1.25.pixels()
        } childOf this

        window.onKeyType { typedChar, keyCode ->
            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                enableSearch()
                searchTextInput.keyType(typedChar, keyCode)
            }
        }
    }

    val searchTextInput by UITextInput("Search...").constrain {
        x = 0.pixel(true)
        y = CenterConstraint()

        width = 15.percent()

    } childOf this

    private val searchIconContainer by UIBlock(ColorCache.brightHighlight.invisible()).constrain {
        x = SiblingConstraint(alignOpposite = true)

        width = AspectConstraint()
        height = 100.percent()
    } childOf this

    init {

        searchTextInput.onKeyType { _, _ ->
            gui.filterPropertyData(searchTextInput.getText())
        }

        searchIconContainer.onMouseEnter {
            animate {
                setColorAnimation(Animations.OUT_EXP, 0.5F, ColorCache.brightHighlight.constraint)
            }
        }.onMouseLeave {
            animate {
                setColorAnimation(Animations.OUT_EXP, 0.5F, ColorCache.brightHighlight.invisible().constraint)
            }
        }

        UIImage.ofURL(URL("https://raw.githubusercontent.com/HappyAndJust/Nameless/master/textures/search.png"))
            .constrain {
                x = CenterConstraint()
                y = CenterConstraint()
                width = 50.percent()
                height = 50.percent()
            } childOf searchIconContainer
            .onLeftClick {
                toggleSearch()
            }
    }

    private fun toggleSearch() {
        if (searchEnabled) disableSearch() else enableSearch()
    }

    private fun enableSearch() {
        if (searchEnabled) return

        searchTextInput.grabWindowFocus()
        if (!searchTextInput.isActive()) {
            searchTextInput.setActive(true)
        }
    }

    private fun disableSearch() {
        if (!searchEnabled) return

        searchTextInput.releaseWindowFocus()
        if (searchTextInput.isActive()) {
            searchTextInput.setActive(false)
        }
    }
}