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

import com.happyandjust.nameless.config.ConfigValue.Companion.configValue
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.OverlayFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.gui.fixed
import com.happyandjust.nameless.gui.relocate.RelocateComponent
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.SkyBlock
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIText
import gg.essential.elementa.dsl.basicTextScaleConstraint
import gg.essential.elementa.dsl.constrain
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

object ShowWitherShieldCoolTime : OverlayFeature("showWitherShieldCoolTime", "Show Wither Shield CoolTime", "", false) {

    init {
        hierarchy {
            +::onlyHeld

            +::onlyCoolTime

            +::precision

            +::readyText

            +::text
        }
    }

    private var onlyHeld by parameter(false) {
        key = "onlyHeld"
        title = "Show only when you are holding Hyperion, Scylla, Valkyrie, Astraea"
    }

    private var onlyCoolTime by parameter(false) {
        key = "onlyCoolTime"
        title = "Show only when wither shield is on cooltime"
    }

    private var precision by parameter(3) {
        key = "precision"
        title = "Decimal Point Precision"
        desc = "Decimal point precision of cooltime"

        settings {
            maxValueInt = 3
        }
    }

    private var readyText by parameter("&aShield: Ready") {
        key = "readyText"
        title = "Overlay Available Text"
        desc = "Text when wither shield is ready"
    }

    private var text by parameter("&6Shield: {value}s") {
        key = "text"
        title = "Overlay Text"
        desc = "Text when wither shield is on cooltime"
    }

    override var overlayPoint by configValue("withershield", "overlay", Overlay.DEFAULT)
    private var lastWitherShieldUse: Long? = null
        get() = field?.takeIf { it + 5000 > System.currentTimeMillis() }
    private val swords = hashSetOf("HYPERION", "VALKYRIE", "SCYLLA", "ASTRAEA")
    private var holdingSword = false
    private val scanTimer = TickTimer(8)

    override fun getRelocateComponent(relocateComponent: RelocateComponent): UIComponent {
        return UIText(readyText.replace("&", "§")).constrain {
            textScale = basicTextScaleConstraint { relocateComponent.currentScale.toFloat() }.fixed()
        }
    }

    override fun shouldDisplayInRelocateGui(): Boolean {
        return enabled && Hypixel.currentGame is SkyBlock
    }

    override fun renderOverlay0(partialTicks: Float) {
        if (enabled && Hypixel.currentGame is SkyBlock) {
            getCooltimeText()?.let {
                matrix {
                    setup(overlayPoint)
                    mc.fontRendererObj.drawStringWithShadow(it.replace("&", "§"), 0f, 0f, 0xFFFFFFFF.toInt())
                }
            }
        }
    }

    private fun getCooltimeText(): String? {
        if (onlyHeld && !holdingSword) {
            return null
        }

        return lastWitherShieldUse?.let {
            val timeLeft = 5 - (System.currentTimeMillis() - it) / 1000.0
            text.replace(
                "{value}",
                timeLeft.withPrecisionText(precision)
            )
        } ?: if (onlyCoolTime) null else readyText
    }

    init {
        on<PacketEvent.Sending>().filter { lastWitherShieldUse == null && Hypixel.currentGame is SkyBlock }
            .subscribe {
                withInstance<C08PacketPlayerBlockPlacement>(packet) {
                    if (stack.getSkyBlockID() in swords) {
                        lastWitherShieldUse = System.currentTimeMillis()
                    }
                }
            }

        on<SpecialTickEvent>().filter { scanTimer.update().check() && Hypixel.currentGame is SkyBlock }
            .subscribe {
                holdingSword = mc.thePlayer.heldItem.getSkyBlockID() in swords
            }
    }
}