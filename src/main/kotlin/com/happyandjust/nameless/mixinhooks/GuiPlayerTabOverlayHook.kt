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

package com.happyandjust.nameless.mixinhooks

import com.happyandjust.nameless.dsl.matrix
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.features.impl.qol.ShowPingInTab
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.client.renderer.GlStateManager.scale
import net.minecraft.client.renderer.GlStateManager.translate
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

object GuiPlayerTabOverlayHook {

    fun drawCustomPing(
        p_175245_1_: Int,
        p_175245_2_: Int,
        p_175245_3_: Int,
        networkPlayerInfoIn: NetworkPlayerInfo,
        ci: CallbackInfo
    ) {
        if (ShowPingInTab.enabled) {
            val x = p_175245_2_ + p_175245_1_ - 13
            val y = p_175245_3_ + mc.fontRendererObj.FONT_HEIGHT / 2f

            matrix {
                translate(x.toFloat(), y, 0f)
                scale(0.8, 0.8, 1.0)
                mc.fontRendererObj.drawStringWithShadow(
                    networkPlayerInfoIn.responseTime.toString(),
                    0f,
                    -(mc.fontRendererObj.FONT_HEIGHT / 2f),
                    ShowPingInTab.color.rgb
                )
            }

            ci.cancel()
        }
    }
}