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

package com.happyandjust.nameless.mixinhooks

import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.features.impl.qol.FeatureShowPingInTab
import net.minecraft.client.network.NetworkPlayerInfo
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import java.awt.Color

object GuiPlayerTabOverlayHook {

    fun drawCustomPing(
        p_175245_1_: Int,
        p_175245_2_: Int,
        p_175245_3_: Int,
        networkPlayerInfoIn: NetworkPlayerInfo,
        ci: CallbackInfo
    ) {
        if (FeatureShowPingInTab.enabled) {
            val x = p_175245_2_ + p_175245_1_ - 13
            val y: Int = p_175245_3_ + mc.fontRendererObj.FONT_HEIGHT / 2

            matrix {
                disableDepth()
                translate(x.toFloat(), y.toFloat(), 0f)
                scale(0.8, 0.8, 1.0)
                mc.fontRendererObj.drawStringWithShadow(
                    networkPlayerInfoIn.responseTime.toString(),
                    0f,
                    -(mc.fontRendererObj.FONT_HEIGHT / 2f),
                    FeatureShowPingInTab.getParameter<Color>("color").value.rgb
                )
                scale(1f, 1f, 1f)
                enableDepth()
            }

            ci.cancel()
        }
    }
}