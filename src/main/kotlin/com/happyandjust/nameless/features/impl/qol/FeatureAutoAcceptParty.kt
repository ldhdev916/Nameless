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

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.config.ConfigValue
import com.happyandjust.nameless.core.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.OverlayFeature
import com.happyandjust.nameless.features.listener.ChatListener
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.KeyInputListener
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.COverlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.dsl.times
import gg.essential.elementa.utils.withAlpha
import net.minecraft.client.gui.Gui
import net.minecraftforge.client.event.ClientChatReceivedEvent
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.regex.Pattern
import kotlin.math.max

object FeatureAutoAcceptParty : OverlayFeature(Category.QOL, "autoacceptparty", "Auto Accept Party", ""), ChatListener,
    KeyInputListener, ClientTickListener {

    init {
        parameters["hide"] = FeatureParameter(
            0,
            "party",
            "hide",
            "Hide Party Request Message",
            "",
            true,
            CBoolean
        )
        parameters["press"] = FeatureParameter(
            0,
            "party",
            "press",
            "Press to Join",
            "Disable automatically join party and instead, Press Y/N to accept/deny party request\nYou can change the key in ESC -> Settings -> Controls",
            false,
            CBoolean
        )
    }

    private val MESSAGE = Pattern.compile(
        """
            -----------------------------
            (\S+\s)?(?<nick>\w+) has invited you to join their party!
            You have 60 seconds to accept. Click here to join!
            -----------------------------
        """.trimIndent()
    )
    private var currentPartyInfo: PartyInfo? = null
    override val overlayPoint = ConfigValue("party", "overlay", Overlay.DEFAULT, COverlay)


    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (!enabled || !mc.thePlayer.inHypixel()) return

        val msg = e.message.unformattedText.stripControlCodes()

        MESSAGE.matchesMatcher(msg) {
            if (getParameterValue("hide")) {
                e.isCanceled = true
            }

            mc.thePlayer.playSound("random.successful_hit", 1F, 0.5F)

            val nickname = it.group("nick")

            if (getParameterValue("press")) {
                currentPartyInfo = PartyInfo(nickname, System.currentTimeMillis() + 5000)
            } else {
                mc.thePlayer.sendChatMessage("/p accept $nickname")
            }
        }
    }

    override fun onKeyInput() {
        currentPartyInfo?.let {
            val keyBinding = Nameless.INSTANCE.keyBindings

            if (keyBinding[KeyBindingCategory.ACCEPT_PARTY]!!.isKeyDown) {
                mc.thePlayer.sendChatMessage("/p accept ${it.nickname}")
                currentPartyInfo = null
            } else if (keyBinding[KeyBindingCategory.DENY_PARTY]!!.isKeyDown) {
                currentPartyInfo = null
            }
        }
    }

    data class PartyInfo(val nickname: String, val expireTime: Long)

    override fun tick() {
        currentPartyInfo?.let {
            if (System.currentTimeMillis() > it.expireTime) currentPartyInfo = null
        }
    }

    override fun shouldDisplayInRelocateGui(): Boolean {
        return enabled && mc.thePlayer.inHypixel() && getParameterValue("press")
    }

    override fun renderOverlay0(partialTicks: Float) {
        currentPartyInfo?.let {
            val keyBindings = Nameless.INSTANCE.keyBindings
            val fontRenderer = mc.fontRendererObj
            val overlay = overlayPoint.value

            val s1 = "§6Party Request From §a${it.nickname}"
            val s2 =
                "§6Press [§a${Keyboard.getKeyName(keyBindings[KeyBindingCategory.ACCEPT_PARTY]!!.keyCode)}§6] to Accept [§c${
                    Keyboard.getKeyName(
                        keyBindings[KeyBindingCategory.DENY_PARTY]!!.keyCode
                    )
                }§6] to Deny"

            val s1Width = fontRenderer.getStringWidth(s1)
            val s2Width = fontRenderer.getStringWidth(s2)

            val maxWidth = max(s1Width, s2Width)
            val height = fontRenderer.FONT_HEIGHT * 4

            matrix {
                translate(overlay.point.x, overlay.point.y, 0)
                scale(overlay.scale, overlay.scale, 1.0)

                Gui.drawRect(0, 0, maxWidth, height, 0x28FFFFFF)

                fontRenderer.drawStringWithShadow(
                    s1,
                    (maxWidth / 2F) - (s1Width / 2F),
                    fontRenderer.FONT_HEIGHT.toFloat(),
                    Color.white.rgb
                )

                fontRenderer.drawStringWithShadow(
                    s2,
                    (maxWidth / 2F) - (s2Width / 2F),
                    (fontRenderer.FONT_HEIGHT * 2).toFloat(),
                    Color.white.rgb
                )
            }
        }
    }

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        val block = UIBlock(Color.white.withAlpha(0.4f)).constrain {
            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint() * 2
        }

        val container = UIContainer().constrain {

            x = CenterConstraint()
            y = CenterConstraint()

            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        } childOf block

        val keyBindings = Nameless.INSTANCE.keyBindings

        val texts = arrayOf(
            "§6Party Request From §aSomeone",
            "§6Press [§a${Keyboard.getKeyName(keyBindings[KeyBindingCategory.ACCEPT_PARTY]!!.keyCode)}§6] to Accept [§c${
                Keyboard.getKeyName(
                    keyBindings[KeyBindingCategory.DENY_PARTY]!!.keyCode
                )
            }§6] to Deny"
        )

        for (text in texts) {
            UIText(text).constrain {
                x = CenterConstraint()
                y = SiblingConstraint()

                textScale = relocateComponent.currentScale.pixels()

                relocateComponent.onScaleChange {
                    textScale = it.pixels()
                }

            } childOf container
        }

        return block
    }
}