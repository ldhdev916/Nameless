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

package com.happyandjust.nameless.features.impl

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.FeatureStateListener
import com.happyandjust.nameless.mixins.accessors.AccessorGuiIngame
import com.happyandjust.nameless.serialization.TypeRegistry
import net.minecraft.client.gui.GuiPlayerTabOverlay
import java.awt.Color

class FeatureShowPingInTab : SimpleFeature(Category.QOL, "pingtab", "Show Ping numbers in Tab"), FeatureStateListener {

    private var prevTabOverlay: GuiPlayerTabOverlay? = null

    init {
        parameters["color"] = FeatureParameter(
            0,
            "pingtab",
            "color",
            "Ping Text Color",
            "",
            Color.green.toChromaColor(),
            TypeRegistry.getConverterByClass(ChromaColor::class)
        )
    }

    override fun onFeatureStateChangePre(e: FeatureStateChangeEvent.Pre) {

    }

    override fun onFeatureStateChangePost(e: FeatureStateChangeEvent.Post) {
        if (e.feature == this) {
            if (e.enabledAfter) {
                prevTabOverlay = mc.ingameGUI.tabList
                (mc.ingameGUI as AccessorGuiIngame).setOverlayPlayerList(GuiPlayerTabOverlay(mc, mc.ingameGUI))
            } else {
                prevTabOverlay?.let {
                    (mc.ingameGUI as AccessorGuiIngame).setOverlayPlayerList(it)
                }
            }
        }
    }
}