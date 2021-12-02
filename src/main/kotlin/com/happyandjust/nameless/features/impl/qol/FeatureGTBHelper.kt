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

package com.happyandjust.nameless.features.impl.qol

import com.google.gson.JsonArray
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.features.listener.ChatListener
import com.happyandjust.nameless.features.listener.ItemTooltipListener
import com.happyandjust.nameless.features.listener.PacketListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorGuiChat
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.init.Items
import net.minecraft.network.play.server.S3APacketTabComplete
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import java.awt.Color
import java.util.regex.Pattern

object FeatureGTBHelper : OverlayFeature(
    Category.QOL,
    "gtbhelper",
    "Guess the Build Helper",
    "Shows possible matching words in screen, also you can press tab to auto complete"
), ItemTooltipListener, ChatListener, PacketListener {

    // english, korean
    private val words = hashMapOf<String, String>()

    fun fetchWordsData() {
        val jsonArray = JsonHandler(ResourceLocation("nameless", "words.json")).read(JsonArray())
        words.putAll(jsonArray.map { it.asJsonObject }.map { it["english"].asString to it["korean"].asString })
    }

    init {
        parameters["translate"] = FeatureParameter(
            0,
            "gtbhelper",
            "translate",
            "Translate Words",
            "Translate all words, themes from english to korean for korean users like me",
            false,
            CBoolean
        )
        parameters["clipboard"] = FeatureParameter(
            1,
            "gtbhelper",
            "clipboard",
            "Copy to Clipboard",
            "When there's only 1 word that matches, copy it to your clipboard",
            false,
            CBoolean
        )
    }

    private val THEME = Pattern.compile("The theme is (?<word>.+)")
    private val matches = arrayListOf<String>()
    private var prevWord: String? = null
    override val overlayPoint = ConfigValue("gtboverlay", "overlay", Overlay.DEFAULT, COverlay)

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (!checkForEnabledAndGuessTheBuild()) return

        if (e.type.toInt() == 2) {
            THEME.matchesMatcher(e.message.unformattedText.stripControlCodes()) {
                val word = it.group("word")
                if (word != prevWord) {
                    prevWord = word

                    matches.clear()

                    if ('_' in word) {
                        matches.addAll(words.keys.filter { s -> s.matches(word.replace("_", "\\w").toRegex()) })

                        if (matches.size == 1 && getParameterValue("clipboard")) {
                            matches[0].apply { sendPrefixMessage("§a$this was copied to your clipboard.") }
                                .copyToClipboard()
                        }
                    }
                }
            }
        }
    }

    override fun onItemTooltip(e: ItemTooltipEvent) {
        if (!checkForEnabledAndGuessTheBuild()) return

        if (getParameterValue("translate")) {
            // Theme Selecting Window
            if (e.itemStack.item == Items.paper && mc.currentScreen is GuiChest) {
                e.toolTip.add(words[e.itemStack.displayName.stripControlCodes()] ?: "NOT FOUND")
            }
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {

        val container = UIContainer().constrain {
            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        }

        repeat(8) {
            UIText("Something Something").constrain {
                y = SiblingConstraint()

                textScale = relocateComponent.currentScale.pixels()

                relocateComponent.onScaleChange {
                    textScale = it.pixels()
                }

                color = Color.red.constraint
            } childOf container
        }

        return container
    }

    override fun shouldDisplayInRelocateGui(): Boolean {
        return checkForEnabledAndGuessTheBuild()
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (!checkForEnabledAndGuessTheBuild()) return

        matrix {
            disableDepth()
            setup(overlayPoint.value)

            var y = 0

            for (english in matches) {
                val builder = StringBuilder(english)

                if (getParameterValue("translate")) {
                    builder.append(" ${words[english] ?: "NOT FOUND"}")
                }

                mc.fontRendererObj.drawString(builder.toString(), 0, y, Color.red.rgb)
                y += mc.fontRendererObj.FONT_HEIGHT
            }

            enableDepth()
        }
    }

    private fun checkForEnabledAndGuessTheBuild() = enabled && Hypixel.currentGame == GameType.GUESS_THE_BUILD

    override fun onSendingPacket(e: PacketEvent.Sending) {

    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
        if (checkForEnabledAndGuessTheBuild()) {
            val msg = e.packet
            val screen = mc.currentScreen

            if (msg is S3APacketTabComplete && screen is AccessorGuiChat) {
                val text = screen.inputField.text

                if (!text.startsWith("/")) {

                    val list = words.keys.filter { matches.contains(it) && it.contains(text, true) }.toMutableList()

                    list.addAll(msg.func_149630_c())

                    e.packet = S3APacketTabComplete(list.toTypedArray())
                }
            }
        }
    }

}