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

import com.happyandjust.nameless.dsl.insertCommaEvery3Character
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.gui.feature.ColorCache
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.effect
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.vigilance.gui.settings.ButtonComponent

class ItemNameContainer(private val gui: AuctionGui, private val auctionInfo: AuctionInfo) :
    UIContainer() {

    val text =
        UIText(
            "${auctionInfo.rarity.colorCode}${
                auctionInfo.item_name.replace(
                    "✪",
                    "§6✪§r"
                )
            } §6${auctionInfo.price.insertCommaEvery3Character()} Coins"
        ).constrain {

            x = 20.pixels()
            y = CenterConstraint()

            textScale = 1.5.pixels()

        } childOf this

    init {
        ButtonComponent("Open Auction") {
            mc.thePlayer.closeScreen()
            mc.thePlayer.sendChatMessage("/viewauction ${auctionInfo.auctionId}")
        }.constrain {
            x = SiblingConstraint(20f)
            y = CenterConstraint()
        } childOf this
    }

    var selected = false
        set(value) {
            field = value

            if (value) {
                text effect OutlineEffect(ColorCache.accent, 1f)

                gui.itemPreviewContainer.setItemPreview(
                    "${auctionInfo.rarity.colorCode}${auctionInfo.item_name}",
                    auctionInfo.lore
                )
            } else {
                text.removeEffect<OutlineEffect>()
            }
        }
}