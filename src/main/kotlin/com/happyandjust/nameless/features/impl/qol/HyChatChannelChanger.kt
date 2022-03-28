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

import com.happyandjust.nameless.config.ConfigValue.Companion.configValue
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.sendPrefixMessage
import com.happyandjust.nameless.dsl.withInstance
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.base.OverlayFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.feature.ColorCache
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.gui.relocate.RelocateGui
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.SkyBlock
import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMatrixStack
import gg.essential.universal.USound
import gg.essential.vigilance.utils.onLeftClick
import net.minecraft.client.gui.GuiChat
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.util.ChatAllowedCharacters
import net.minecraftforge.client.event.GuiScreenEvent
import org.lwjgl.input.Mouse

object HyChatChannelChanger : OverlayFeature(
    "hyChatChannelChanger",
    "HyChat Channel Changer",
    "Add button where you could select chat channel like party, guild, reply in hypixel"
) {

    init {
        hierarchy {
            +::exceptionPrefix

            +::selectedPrefixTypes
        }
    }

    override var overlayPoint by configValue("hychat", "overlay", Overlay.DEFAULT)
    private var currentPrefix by configValue("hychat", "currentPrefix", "/ac")
    private val channelButtons = ChannelsContainer.children.filterIsInstance<ChannelButton>()
    private val window = Window(ElementaVersion.V1).apply { ChannelsContainer childOf this }

    private var exceptionPrefix by parameter("!") {
        key = "exceptionPrefix"
        title = "Exception Prefix"
        desc =
            "If you write this prefix at the first of your chat message, that message will be prevented from going to channel you selected\n§lSet this to empty if you don't want this feature"

        settings {
            validator = ChatAllowedCharacters::isAllowedCharacter
        }
    }

    private var selectedPrefixTypes by parameter(PrefixType.values().toList()) {
        key = "selectedPrefixTypes"
        title = "Selected Chat Types"

        settings {
            ordinal = 1
            autoFillEnum { it.prettyName }
        }
    }

    init {

        on<GuiScreenEvent.InitGuiEvent.Post>().filter { gui is GuiChat }.subscribe {
            ChannelsContainer.buttonsMap.forEach { (type, button) ->
                if (type in selectedPrefixTypes && type.specialReq()) {
                    button.unhide()
                } else {
                    button.hide()
                }
            }
            ChannelsContainer.children.sortBy {
                ChannelsContainer.buttonsMap.values.indexOf(it)
            }
        }

        on<GuiScreenEvent.DrawScreenEvent.Post>().filter {
            enabled && EssentialAPI.getMinecraftUtil().isHypixel() && gui is GuiChat
        }.subscribe {
            window.draw(UMatrixStack.Compat.get())
        }

        on<GuiScreenEvent.MouseInputEvent.Post>().filter {
            enabled && EssentialAPI.getMinecraftUtil().isHypixel() && gui is GuiChat
        }.subscribe {
            val mouseX = Mouse.getEventX().toDouble() * gui.width / mc.displayWidth
            val mouseY = gui.height - 1 - (Mouse.getEventY().toDouble() * gui.height / mc.displayHeight)
            val button = Mouse.getEventButton()
            if (Mouse.getEventButtonState()) {
                window.mouseClick(mouseX, mouseY, button)
            } else {
                window.mouseRelease()
            }
        }

        on<PacketEvent.Sending>().filter { enabled && EssentialAPI.getMinecraftUtil().isHypixel() }.subscribe {
            withInstance<C01PacketChatMessage>(packet) {
                if (message.startsWith("/")) return@subscribe
                packet = C01PacketChatMessage(
                    if (exceptionPrefix.isNotBlank() && message.startsWith(exceptionPrefix)) {
                        message.substringAfter(exceptionPrefix)
                    } else {
                        "$currentPrefix $message"
                    }
                )
            }
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        val container = UIContainer().constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedMaxSizeConstraint()
        }

        val constraint = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()

        selectedPrefixTypes.forEach {
            if (it.specialReq()) {
                ChannelButton(it.prettyName, it.messagePrefix, constraint) childOf container
            }
        }

        return container
    }

    override fun shouldDisplayInRelocateGui() = enabled && EssentialAPI.getMinecraftUtil().isHypixel()

    override fun renderOverlay0(partialTicks: Float) = Unit

    object ChannelsContainer : UIContainer() {
        val buttonsMap = PrefixType.values().associateWith {
            ChannelButton(
                it.prettyName,
                it.messagePrefix,
                basicTextScaleConstraint { overlayPoint.scale.toFloat() }.fixed()
            ) childOf this
        }

        init {

            constrain {
                x = basicXConstraint { overlayPoint.x.toFloat() }.fixed()
                y = basicYConstraint { overlayPoint.y.toFloat() }.fixed()

                width = ChildBasedSizeConstraint()
                height = ChildBasedMaxSizeConstraint()
            }
        }
    }

    class ChannelButton(
        private val text: String,
        private val messagePrefix: String,
        textScaleConstraint: HeightConstraint
    ) : UIRoundedRectangle(6f) {

        private val selected
            get() = messagePrefix == currentPrefix

        private val originColor
            get() = if (selected) ColorCache.darkHighlight.withAlpha(0.5f) else ColorCache.darkHighlight

        init {
            constrain {
                x = SiblingConstraint(5f)

                width = ChildBasedSizeConstraint() * 2
                height = ChildBasedSizeConstraint() * 1.3

                color = originColor.constraint
            }

            UIText(text).constrain {
                x = CenterConstraint()
                y = CenterConstraint()

                textScale = textScaleConstraint
            }.onLeftClick {
                if (mc.currentScreen !is RelocateGui) {
                    USound.playButtonPress()
                    select()
                }
            } childOf this

            onMouseEnter {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.darkHighlight.withAlpha(0.7f).constraint)
                }
            }

            onMouseLeave {
                animate {
                    setColorAnimation(Animations.OUT_EXP, .5f, originColor.constraint)
                }
            }
        }

        private fun select() = animate {
            if (selected) return@animate
            sendPrefixMessage("Changed to §6$text!")
            setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.darkHighlight.withAlpha(0.5f).constraint)

            (channelButtons - this@ChannelButton).forEach { it.deselect() }
            currentPrefix = messagePrefix
        }

        private fun deselect() = animate {
            if (!selected) return@animate
            setColorAnimation(Animations.OUT_EXP, .5f, ColorCache.darkHighlight.constraint)
        }
    }

    enum class PrefixType(val prettyName: String, val messagePrefix: String, val specialReq: () -> Boolean = { true }) {
        ALL("All", "/ac"),
        GUILD("Guild", "/gc"),
        PARTY("Party", "/pc"),
        REPLY("Reply", "/r"),
        OFFICIAL("Official", "/oc"),
        CO_OP(
            "Co-Op",
            "/cc",
            { Hypixel.currentGame is SkyBlock }
        );
    }
}