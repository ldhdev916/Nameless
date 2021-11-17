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

package com.happyandjust.nameless.gui.auction

import com.happyandjust.nameless.gui.feature.ColorCache
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.universal.GuiScale
import gg.essential.vigilance.gui.settings.SelectorComponent
import gg.essential.vigilance.utils.onLeftClick

class AuctionGui(auctionInfos: List<AuctionInfo>) :
    WindowScreen(newGuiScale = GuiScale.scaleForScreenSize().ordinal) {

    private var miniumRarity = ItemRarity.COMMON

    init {
        UIBlock(ColorCache.background).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf window
    }

    private val content = UIContainer().constrain {

        x = CenterConstraint()
        y = CenterConstraint()

        width = 85.percent()
        height = 75.percent()
    } childOf window

    private val searchBar = UIContainer().constrain {
        width = 100.percent()
        height = 30.pixels()
    } childOf content

    private val textInput = UITextInput("Search...").constrain {
        x = 10.pixels()
        y = CenterConstraint()

        width = 15.percent()
    } childOf searchBar

    init {

        textInput.onLeftClick {
            grabWindowFocus()
        }.onKeyType { _, _ ->
            displayAuctionItemsScroller(
                auctionInfos.filter {
                    it.item_name.contains(textInput.getText(), true)
                }
            )
        }

        UIText("Minimum Rarity:").constrain {
            x = SiblingConstraint(5f)
            y = CenterConstraint()
        } childOf searchBar

        val rarities = ItemRarity.values()

        val selector = SelectorComponent(rarities.indexOf(miniumRarity), rarities.map { it.loreName }).constrain {
            x = SiblingConstraint(5f)
            y = 0.pixel()
        } childOf searchBar

        selector.onValueChange {
            miniumRarity = rarities[it as Int]
            displayAuctionItemsScroller(auctionInfos.filter { it.item_name.contains(textInput.getText(), true) })
        }
    }

    private val mainContent = UIContainer().constrain {

        y = SiblingConstraint()

        width = 100.percent()
        height = FillConstraint(false)
    } childOf content


    init {
        UIBlock(ColorCache.divider).constrain {
            width = 1.pixel()
            height = 100.percent()
        } childOf content

        UIBlock(ColorCache.divider).constrain {
            x = 0.pixel(alignOpposite = true)

            width = 1.pixel()
            height = 100.percent()
        } childOf content
    }

    private val auctionItemsScrollerContainer = UIContainer().constrain {
        width = 65.percent()
        height = 100.percent()
    } childOf mainContent

    private var auctionItemsScoller = AuctionItemsScoller(
        this,
        auctionInfos.filter { it.rarity >= miniumRarity }) childOf auctionItemsScrollerContainer

    private fun displayAuctionItemsScroller(auctionInfos: List<AuctionInfo>) {

        auctionItemsScoller.hide()
        auctionItemsScoller =
            AuctionItemsScoller(
                this,
                auctionInfos.filter { it.rarity >= miniumRarity }) childOf auctionItemsScrollerContainer
    }

    init {
        UIBlock(ColorCache.divider).constrain {
            y = (-.5f).pixels()
            x = SiblingConstraint()

            width = 1.pixel()
            height = 100.percent() + .5f.pixels()
        } childOf mainContent
    }

    val itemPreviewContainer = ItemPreviewContainer().constrain {

        x = SiblingConstraint()

        width = FillConstraint(false)
        height = 100.percent()
    } childOf mainContent

}