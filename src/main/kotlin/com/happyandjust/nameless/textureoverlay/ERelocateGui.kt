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

package com.happyandjust.nameless.textureoverlay

import com.happyandjust.nameless.core.Point
import com.happyandjust.nameless.devqol.matrix
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution

class ERelocateGui(
    private val relocatablePanel: ERelocatablePanel,
    private val saveOverlay: (Overlay) -> Unit
) : GuiScreen() {


    override fun onGuiClosed() {
        saveOverlay(
            Overlay(
                Point(relocatablePanel.rectangle.left, relocatablePanel.rectangle.top),
                relocatablePanel.scale
            )
        )
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val scaleFactor = ScaledResolution(mc).also { relocatablePanel.sr = it }.scaleFactor

        matrix {
            relocatablePanel.drawPanel(mouseX, mouseY, scaleFactor)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        relocatablePanel.mousePressed0(mouseX, mouseY)
    }
}