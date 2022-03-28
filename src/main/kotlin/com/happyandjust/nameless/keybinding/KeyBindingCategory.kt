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

package com.happyandjust.nameless.keybinding

import com.happyandjust.nameless.MOD_NAME
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.lwjgl.input.Keyboard

enum class KeyBindingCategory(val desc: String, val key: Int) {

    OPEN_GUI("Open Gui", Keyboard.KEY_V),
    PERSPECTIVE("Perspective", Keyboard.KEY_C),
    FREEZE_FAIRYSOUL_PATHS("Freeze FairySoul Paths", Keyboard.KEY_NONE),
    GHOST_BLOCK("Ghost Block", Keyboard.KEY_X),
    ACCEPT_PARTY("Accept Party", Keyboard.KEY_Y),
    DENY_PARTY("Deny Party", Keyboard.KEY_N),
    FREEZE_WAYPOINT_PATH("Freeze Waypoint Paths", Keyboard.KEY_NONE);

    val keyBinding = KeyBinding(desc, key, MOD_NAME).also { ClientRegistry.registerKeyBinding(it) }
}