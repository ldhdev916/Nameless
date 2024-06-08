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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import com.happyandjust.nameless.utils.ScoreboardUtils
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMatrixStack
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumChatFormatting
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.common.util.Constants
import java.awt.Color
import java.util.regex.Matcher
import java.util.regex.Pattern

object BazaarHelper : SimpleFeature(
    "bazaarHelper",
    "Bazaar Helper",
    "Shows some information to your screen while bazaar gui is open"
) {
    private const val GLOBAL_TEXT_SCALE = 1f
    private val window = Window(ElementaVersion.V5).apply {
        BazaarContainer childOf this
    }

    private val pricePerUnit = "Price per unit: (?<coin>(\\d|\\.)+) coins".toPattern()
    private val buyOrder =
        "- (?<coin>(\\d|\\.)+) coins each \\| (?<units>\\d+)x in (?<orders>\\d+) order(s)?".toPattern()
    private val sellOrder =
        "- (?<coin>(\\d|\\.)+) coins each \\| (?<units>\\d+)x from (?<offers>\\d+) offer(s)?".toPattern()
    //§8- §6203,887.2 coins §7each | §a9§7x §7in §f1 §7order

    init {
        on<GuiScreenEvent.InitGuiEvent.Post>().filter { enabled && Hypixel.currentGame == GameType.SKYBLOCK && gui is GuiChest }
            .subscribe {
                gui.withInstance<AccessorGuiContainer> {

                    BazaarInformation.entries.forEach { it.updateValue(Double.NaN, "") }

                    BazaarContainer.constrain {
                        x = (window.getRight() - guiLeft).pixels(true)

                        height = (ySize * 0.9).pixels()

                        y = (guiTop + ySize / 2 - getHeight() / 2).pixels()
                    }
                }
            }

        on<GuiScreenEvent.BackgroundDrawnEvent>().filter { gui.shouldDisplay() }.subscribe {
            window.draw(UMatrixStack.Compat.get())
        }

        on<SpecialTickEvent>().subscribe {
            mc.currentScreen.withInstance<GuiChest> { updateDisplay() }
        }
    }

    private fun GuiScreen.shouldDisplay(): Boolean {
        if (!enabled || Hypixel.currentGame != GameType.SKYBLOCK || this !is GuiChest) return false
        inventorySlots.withInstance<ContainerChest> {
            val slots = inventorySlots.filter { it.inventory != mc.thePlayer.inventory }
            if (slots.size != 36) return false
            val name = lowerChestInventory.displayName.unformattedText.stripControlCodes()

            return slots[10].stack?.displayName?.stripControlCodes() == "Buy Instantly" || name in arrayOf(
                "How many do you want?",
                "At what price are you selling?"
            )
        }
        return false
    }

    private fun GuiScreen.updateDisplay() {
        if (!enabled || Hypixel.currentGame != GameType.SKYBLOCK || this !is GuiChest) return
        inventorySlots.withInstance<ContainerChest> {
            val slots = inventorySlots.filter { it.inventory != mc.thePlayer.inventory }
            if (slots.size != 36 || slots[10].stack?.displayName?.stripControlCodes() != "Buy Instantly") return

            val coinLine =
                ScoreboardUtils.getSidebarLines(true).find { it.startsWith("Purse:") || it.startsWith("Piggy:") }
                    ?: return
            val coin = coinLine.split(":")[1].trim().replace(",", "").toDoubleOrNull()

            slots[13].stack?.displayName?.let {
                BazaarContainer.itemName.setText(it)
            }

            slots[10].stack.whenMatches(pricePerUnit) {
                val instantBuyPrice = it.group("coin").toDouble()
                BazaarInformation.INSTANT_BUY_PRICE.updateValue(instantBuyPrice, "Coin")

                if (coin != null) {
                    BazaarInformation.INSTANT_BUYABLE.updateValue((coin / instantBuyPrice).toInt(), "Item")
                }
            }

            slots[11].stack.whenMatches(pricePerUnit) {
                BazaarInformation.INSTANT_SELL_PRICE.updateValue(it.group("coin").toDouble(), "Coin")
            }


            slots[15].stack.whenMatches(buyOrder) {
                val buyPrice = it.group("coin").toDouble()

                BazaarInformation.BUY_ORDER_PRICE_PER_UNIT.updateValue(buyPrice, "Coin")
                BazaarInformation.BUY_ORDER_AMOUNT_OF_UNITS.updateValue(it.group("units").toInt(), "Unit")
                BazaarInformation.BUY_ORDER_AMOUNT_OF_ORDERS.updateValue(it.group("orders").toInt(), "Order")

                if (coin != null) {
                    BazaarInformation.BUY_ORDER_BUYABLE.updateValue((coin / buyPrice).toInt(), "Item")
                }
            }

            slots[16].stack.whenMatches(sellOrder) {
                BazaarInformation.SELL_ORDER_PRICE_PER_UNIT.updateValue(it.group("coin").toDouble(), "Coin")
                BazaarInformation.SELL_ORDER_AMOUNT_OF_UNITS.updateValue(it.group("units").toInt(), "Unit")
                BazaarInformation.SELL_ORDER_AMOUNT_OF_OFFERS.updateValue(it.group("offers").toInt(), "Offer")
            }
        }
    }

    private inline fun ItemStack?.whenMatches(pattern: Pattern, action: (Matcher) -> Unit) {
        getLores().map { pattern.matcher(it) }.find { it.matches() }?.let(action)
    }

    private fun ItemStack?.getLores(): List<String> {
        this ?: return emptyList()

        if (!hasTagCompound()) return emptyList()
        return runCatching {
            val tagList = tagCompound.getCompoundTag("display").getTagList("Lore", Constants.NBT.TAG_STRING)

            List(tagList.tagCount()) { tagList.getStringTagAt(it).trim().stripControlCodes().replace(",", "") }
        }.getOrElse { emptyList() }
    }

    object BazaarContainer : UIRoundedRectangle(2f) {
        private val wholeContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            width = 90.percent()
            height = ChildBasedSizeConstraint()
        } childOf this

        val itemName = UIText().constrain {
            textScale = GLOBAL_TEXT_SCALE.pixels()
        } childOf wholeContainer

        val instantContainer = UIContainer().constrain {

            y = SiblingConstraint(8f)

            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf wholeContainer

        val buyOrderContainer = UIContainer().constrain {

            y = SiblingConstraint(8f)

            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf wholeContainer

        val sellOrderContainer = UIContainer().constrain {

            y = SiblingConstraint(8f)

            width = 100.percent()
            height = ChildBasedSizeConstraint()
        } childOf wholeContainer

        init {

            setColor(Color.black.withAlpha(.7f))

            constrain {
                width = 200.pixels()
            }

            UIText("§dBuy Orders").constrain {
                textScale = GLOBAL_TEXT_SCALE.pixels()
            } childOf buyOrderContainer

            UIText("§dSell Orders").constrain {
                textScale = GLOBAL_TEXT_SCALE.pixels()
            } childOf sellOrderContainer

            BazaarInformation.entries.forEach {
                it.textComponent childOf it.parentContainer
                it.updateValue(Double.NaN, "")
            }
        }
    }

    enum class BazaarInformation(
        val parentContainer: UIContainer,
        infoText: String,
        private val valueChatColor: EnumChatFormatting
    ) {

        INSTANT_BUY_PRICE(BazaarContainer.instantContainer, "Instant Buy Price", EnumChatFormatting.GOLD),
        INSTANT_SELL_PRICE(BazaarContainer.instantContainer, "Instant Sell Price", EnumChatFormatting.GOLD),
        INSTANT_BUYABLE(BazaarContainer.instantContainer, "§bBuyable", EnumChatFormatting.AQUA),

        BUY_ORDER_AMOUNT_OF_ORDERS(BazaarContainer.buyOrderContainer, "Amount of Orders", EnumChatFormatting.DARK_AQUA),
        BUY_ORDER_PRICE_PER_UNIT(BazaarContainer.buyOrderContainer, "Price Per Unit", EnumChatFormatting.GOLD),
        BUY_ORDER_AMOUNT_OF_UNITS(BazaarContainer.buyOrderContainer, "Amount of Units", EnumChatFormatting.DARK_AQUA),
        BUY_ORDER_BUYABLE(BazaarContainer.buyOrderContainer, "§bBuyable with Orders", EnumChatFormatting.AQUA),

        SELL_ORDER_AMOUNT_OF_OFFERS(
            BazaarContainer.sellOrderContainer,
            "Amount of Offers",
            EnumChatFormatting.DARK_AQUA
        ),
        SELL_ORDER_PRICE_PER_UNIT(BazaarContainer.sellOrderContainer, "Price Per Unit", EnumChatFormatting.GOLD),
        SELL_ORDER_AMOUNT_OF_UNITS(BazaarContainer.sellOrderContainer, "Amount of Units", EnumChatFormatting.DARK_AQUA);

        val textComponent = PairTextComponent(infoText).constrain {
            y = SiblingConstraint()

            width = 100.percent()
            height = ChildBasedMaxSizeConstraint()
        }

        fun updateValue(number: Number, suffix: String) {
            if (number.toDouble().isNaN()) {
                textComponent.right.setText("$valueChatColor$number")
                return
            }
            var text = number.toDouble().formatDouble()

            text = if ("." in text) {
                val (realNum, digit) = text.split(".")

                realNum.reversed().chunked(3).joinToString(",").reversed() + ".$digit"
            } else {
                text.reversed().chunked(3).joinToString(",").reversed()
            }

            textComponent.right.setText("$valueChatColor$text ${if (text == "1") suffix else "${suffix}s"}")
        }

        class PairTextComponent(infoText: String) : UIContainer() {

            val left = UIText("$infoText:").constrain {
                textScale = GLOBAL_TEXT_SCALE.pixels()
            } childOf this

            val right = UIText().constrain {
                x = 0.pixel(true)

                textScale = GLOBAL_TEXT_SCALE.pixels()
            } childOf this
        }
    }
}