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

package com.happyandjust.nameless.listener

import com.happyandjust.nameless.dsl.LOGGER
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.KeyPressEvent
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.gui.feature.FeatureGui
import com.happyandjust.nameless.keybinding.KeyBindingCategory
import gg.essential.api.utils.GuiUtil
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

object BasicListener {

    private val prevPressed = hashMapOf<KeyBindingCategory, Boolean>().withDefault { false }

    init {
        on<KeyPressEvent>().filter { isNew && !inGui && keyBindingCategory == KeyBindingCategory.OPEN_GUI }.subscribe {
            GuiUtil.open(FeatureGui())
        }

        val realKeyDown: KeyBinding.() -> Boolean = {
            when {
                keyCode == Keyboard.KEY_NONE -> false
                keyCode < 0 -> Mouse.isButtonDown(keyCode + 100)
                else -> Keyboard.isKeyDown(keyCode)
            }
        }

        on<SpecialTickEvent>().subscribe {
            for (keyBindingCategory in KeyBindingCategory.values()) {
                val pressed = keyBindingCategory.keyBinding.realKeyDown()
                if (pressed) {
                    MinecraftForge.EVENT_BUS.post(
                        KeyPressEvent(
                            keyBindingCategory,
                            !prevPressed.getValue(keyBindingCategory),
                            mc.currentScreen != null
                        )
                    )
                }
                prevPressed[keyBindingCategory] = pressed
            }
        }

        on<FMLNetworkEvent.ClientConnectedToServerEvent>().subscribe {
            manager.channel().pipeline().addBefore("packet_handler", "nameless_packet_handler", PacketHandler())
            LOGGER.info("Added Packet Handler")
        }

        on<TickEvent.ClientTickEvent>().filter { phase == TickEvent.Phase.END && mc.theWorld != null && mc.thePlayer != null }
            .subscribe {
                MinecraftForge.EVENT_BUS.post(SpecialTickEvent())
            }

        on<RenderGameOverlayEvent.Post>().filter { type == RenderGameOverlayEvent.ElementType.ALL }.subscribe {
            MinecraftForge.EVENT_BUS.post(SpecialOverlayEvent(partialTicks))
            color(1f, 1f, 1f, 1f)
        }
    }
}