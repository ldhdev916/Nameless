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
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.OverlayFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.hide
import com.happyandjust.nameless.features.press
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import gg.essential.api.EssentialAPI
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedMaxSizeConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.invisible
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.UMatrixStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

object AutoAcceptParty : OverlayFeature("autoAcceptParty", "Auto Accept Party") {

    init {
        parameter(true) {
            matchKeyCategory()
            key = "hide"
            title = "Hide Party Request Message"
        }

        parameter(false) {
            matchKeyCategory()
            key = "press"
            title = "Press to Join"
            desc =
                "Disable automatically join party and instead, Press Y/N to accept/deny party request\nYou can change the key in ESC -> Settings -> Controls"
        }
    }

    private val MESSAGE = """
            -----------------------------
            (\S+\s)?(?<nick>\w+) has invited you to join their party!
            You have 60 seconds to accept. Click here to join!
            -----------------------------
        """.trimIndent().toPattern()

    private var currentPartyInfo: PartyInfo? = null
        set(value) {
            field = value
            if (value != null) {
                PartyOverlayContainer.update(value)
                shouldDraw = true
            }
            PartyOverlayContainer.animate(value != null)
        }
    private var shouldDraw = false
    override var overlayPoint by configValue("party", "overlay", Overlay.DEFAULT)

    private val window = Window(ElementaVersion.V5)

    init {
        on<ClientChatReceivedEvent>().filter {
            enabled && EssentialAPI.getMinecraftUtil().isHypixel() && type.toInt() != 2
        }.subscribe {
            MESSAGE.matchesMatcher(pureText) {
                if (hide) cancel()

                mc.thePlayer.playSound("random.successful_hit", 1F, 0.5F)

                val nickname = group("nick")

                if (press) {
                    currentPartyInfo = PartyInfo(nickname, System.currentTimeMillis() + 5000)
                } else {
                    mc.thePlayer.sendChatMessage("/p accept $nickname")
                }
            }
        }

        on<InputEvent.KeyInputEvent>().subscribe {
            currentPartyInfo?.let {
                if (KeyBindingCategory.ACCEPT_PARTY.getKeyBinding().isKeyDown) {
                    mc.thePlayer.sendChatMessage("/p accept ${it.nickname}")
                    currentPartyInfo = null
                } else if (KeyBindingCategory.DENY_PARTY.getKeyBinding().isKeyDown) {
                    currentPartyInfo = null
                }
            }
        }

        on<SpecialTickEvent>().subscribe {
            currentPartyInfo?.let {
                if (System.currentTimeMillis() > it.expireTime) currentPartyInfo = null
            }
        }
    }

    data class PartyInfo(val nickname: String, val expireTime: Long)

    override fun shouldDisplayInRelocateGui(): Boolean {
        return enabled && EssentialAPI.getMinecraftUtil().isHypixel() && press
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (shouldDraw) {
            window.draw(UMatrixStack.Compat.get())
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

        val texts = arrayOf(
            "§6Party Request From §aSomeone",
            "§6Press [§a${getKeyName(KeyBindingCategory.ACCEPT_PARTY)}§6] to Accept [§c${getKeyName(KeyBindingCategory.DENY_PARTY)}§6] to Deny"
        )

        for (text in texts) {
            UIText(text).constrain {
                x = CenterConstraint()
                y = SiblingConstraint()

                textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()

            } childOf container
        }

        return block
    }

    private fun getKeyName(keyBindingCategory: KeyBindingCategory) =
        Keyboard.getKeyName(keyBindingCategory.getKeyBinding().keyCode)

    private object PartyOverlayContainer : UIContainer() {

        init {
            childOf(window)
        }

        private val block = UIBlock(Color.white.invisible()).constrain {

            x = basicXConstraint { overlayPoint.x.toFloat() }.fixed()
            y = basicYConstraint { overlayPoint.y.toFloat() }.fixed()

            width = ChildBasedSizeConstraint()
            height = ChildBasedSizeConstraint() * 2
        } childOf this

        val container = UIContainer().constrain {
            x = CenterConstraint()
            y = CenterConstraint()

            width = ChildBasedMaxSizeConstraint()
            height = ChildBasedSizeConstraint()
        } childOf block

        val text1 = UIText().constrain {
            x = CenterConstraint()

            textScale = basicTextScaleConstraint { overlayPoint.scale.toFloat() }.fixed()
            color = Color.white.invisible().constraint
        } childOf container

        val text2 = UIText().constrain {
            x = CenterConstraint()
            y = SiblingConstraint()

            textScale = basicTextScaleConstraint { overlayPoint.scale.toFloat() }.fixed()
            color = Color.white.invisible().constraint
        } childOf container

        fun update(partyInfo: PartyInfo) {
            arrayOf(block, text1, text2).forEach {
                it.setColor(Color.white.invisible())
            }

            text1.setText("§6Party Request From §a${partyInfo.nickname}")

            text2.setText(
                "§6Press [§a${getKeyName(KeyBindingCategory.ACCEPT_PARTY)}§6] to Accept [§c${
                    getKeyName(KeyBindingCategory.DENY_PARTY)
                }§6] to Deny"
            )

        }

        fun animate(draw: Boolean) {
            val time = 1f
            if (draw) {
                arrayOf(text1, text2).forEach {
                    it.animate {
                        setColorAnimation(Animations.OUT_EXP, time, Color.white.constraint)
                    }
                }
                block.animate {
                    setColorAnimation(Animations.OUT_EXP, time, Color.white.withAlpha(0.4f).constraint)
                }
            } else {
                arrayOf(block, text1, text2).forEach {
                    it.animate {
                        setColorAnimation(Animations.OUT_EXP, time, Color.white.invisible().constraint)

                        if (it == text2) {
                            onComplete {
                                shouldDraw = false
                            }
                        }
                    }
                }
            }
        }
    }
}