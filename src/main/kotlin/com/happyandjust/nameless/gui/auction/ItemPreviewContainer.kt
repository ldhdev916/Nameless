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

package com.happyandjust.nameless.gui.auction

import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIWrappedText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.percent
import gg.essential.elementa.dsl.pixels

class ItemPreviewContainer : UIContainer() {

    private val scroller = ScrollComponent(innerPadding = 10f, customScissorBoundingBox = this).constrain {
        width = 100.percent()
        height = 100.percent()
    } childOf this

    private val tooltipTextContainer = UIContainer().constrain {
        width = FillConstraint(false)
        height = ChildBasedSizeConstraint()
    } childOf scroller

    fun setItemPreview(displayName: String, lore: String) {
        tooltipTextContainer.children.clear()

        UIWrappedText(displayName.replace("✪", "§6✪§r")).constrain {

            width = 100.percent()

            textScale = 1.5.pixels()
        } childOf tooltipTextContainer

        UIWrappedText(lore).constrain {
            y = SiblingConstraint(4f)

            width = 100.percent()
        } childOf tooltipTextContainer
    }
}