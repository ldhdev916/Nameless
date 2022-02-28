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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.config.configValue
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.base.OverlayFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.clipboard
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.features.translate
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.mixins.accessors.AccessorGuiChat
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager.disableDepth
import net.minecraft.client.renderer.GlStateManager.enableDepth
import net.minecraft.init.Items
import net.minecraft.network.play.server.S3APacketTabComplete
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import java.awt.Color

object GTBHelper : OverlayFeature(
    "gtbHelper",
    "Guess the Build Helper",
    "Shows possible matching words in screen, also you can press tab to auto complete"
) {

    // english, korean
    @OptIn(ExperimentalSerializationApi::class)
    private val words =
        Json.decodeFromStream<List<Map<String, String>>>(ResourceLocation("nameless", "words.json").inputStream())
            .associate {
                it["english"]!! to it["korean"]!!
            }

    init {
        parameter(false) {
            matchKeyCategory()
            key = "translate"
            title = "Translate Words"
            desc = "Translate all words, themes from english to korean for korean users like me"
        }

        parameter(false) {
            matchKeyCategory()
            key = "clipboard"
            title = "Copy to Clipboard"
            desc = "When there's only 1 word that matches, copy it to your clipboard"

            settings {
                ordinal = 1
            }
        }
    }

    private val THEME = "The theme is (?<word>.+)".toPattern()
    private val matches = arrayListOf<String>()
    private var prevWord: String? = null
    override var overlayPoint by configValue("gtboverlay", "overlay", Overlay.DEFAULT)

    init {
        on<ClientChatReceivedEvent>().filter { checkForEnabledAndGuessTheBuild() && type.toInt() == 2 }.subscribe {
            THEME.matchesMatcher(pureText) {
                val word = group("word")
                if (word != prevWord) {
                    prevWord = word

                    matches.clear()

                    if ('_' in word) {
                        matches.addAll(words.keys.filter { s -> s.matches(word.replace("_", "\\w").toRegex()) })

                        if (matches.size == 1 && clipboard) {
                            matches[0].apply { sendPrefixMessage("§a$this was copied to your clipboard.") }
                                .copyToClipboard()
                        }
                    }
                }
            }
        }

        on<ItemTooltipEvent>().filter { checkForEnabledAndGuessTheBuild() && translate && itemStack.item == Items.paper && mc.currentScreen is GuiChest }
            .subscribe {
                toolTip.add(words[itemStack.displayName.stripControlCodes()] ?: "NOT FOUND")
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

                textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()

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
            setup(overlayPoint)

            var y = 0

            for (english in matches) {
                val builder = StringBuilder(english)

                if (translate) {
                    builder.append(" ${words[english] ?: "NOT FOUND"}")
                }

                mc.fontRendererObj.drawString(builder.toString(), 0, y, Color.red.rgb)
                y += mc.fontRendererObj.FONT_HEIGHT
            }

            enableDepth()
        }
    }

    private fun checkForEnabledAndGuessTheBuild() = enabled && Hypixel.currentGame == GameType.GUESS_THE_BUILD

    init {
        on<PacketEvent.Received>().filter { checkForEnabledAndGuessTheBuild() }.subscribe {
            packet.withInstance<S3APacketTabComplete> {
                mc.currentScreen.withInstance<AccessorGuiChat> {
                    val text = inputField.text

                    if (!text.startsWith("/")) {
                        packet = S3APacketTabComplete(words.keys.filter { it in matches && it.contains(text, true) }
                            .toTypedArray())
                    }
                }
            }
        }
    }

}