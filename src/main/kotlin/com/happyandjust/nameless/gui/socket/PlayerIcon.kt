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

package com.happyandjust.nameless.gui.socket

import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ImageAspectConstraint
import gg.essential.elementa.constraints.MousePositionConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import java.net.URL

class PlayerIcon(playerName: String) : UIContainer() {

    init {
        val nameText = UIText(playerName).constrain {
            x = MousePositionConstraint()
            y = MousePositionConstraint()
        }

        UIImage.ofURL(URL("https://mc-heads.net/avatar/happyandjust")).constrain {
            width = 100.percent()
            height = ImageAspectConstraint()
        }.onMouseEnter {
            Window.enqueueRenderOperation {
                nameText childOf Window.of(this@PlayerIcon)
            }
        }.onMouseLeave {
            Window.enqueueRenderOperation {
                Window.of(this@PlayerIcon).removeChild(nameText)
            }
        } childOf this
    }
}