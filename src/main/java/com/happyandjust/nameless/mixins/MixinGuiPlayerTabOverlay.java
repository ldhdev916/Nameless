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

package com.happyandjust.nameless.mixins;

import com.happyandjust.nameless.features.FeatureRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    @Shadow
    @Final
    private Minecraft mc;

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    public void drawCustomPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn, CallbackInfo ci) {
        if (FeatureRegistry.INSTANCE.getSHOW_PING_NUMBER_IN_TAB().getEnabled()) {

            int x = p_175245_2_ + p_175245_1_ - 13;
            int y = p_175245_3_ + (mc.fontRendererObj.FONT_HEIGHT / 2);

            GlStateManager.pushMatrix();
            GlStateManager.disableDepth();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(0.8, 0.8, 1);

            mc.fontRendererObj.drawStringWithShadow(String.valueOf(networkPlayerInfoIn.getResponseTime()), 0, -(mc.fontRendererObj.FONT_HEIGHT / 2F), ((Color) FeatureRegistry.INSTANCE.getSHOW_PING_NUMBER_IN_TAB().getParameter("color").getValue()).getRGB());

            GlStateManager.scale(1, 1, 1);
            GlStateManager.enableDepth();
            GlStateManager.popMatrix();

            ci.cancel();
        }
    }
}
