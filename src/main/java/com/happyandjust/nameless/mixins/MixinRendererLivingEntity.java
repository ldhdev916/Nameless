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

package com.happyandjust.nameless.mixins;

import com.happyandjust.nameless.features.impl.misc.ChangeDamagedEntityColor;
import com.happyandjust.nameless.mixinhooks.RenderGlobalHook;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {

    @Unique
    private final RenderGlobalHook hook = RenderGlobalHook.INSTANCE;

    @Inject(method = "setScoreTeamColor", at = @At("HEAD"), cancellable = true)
    public void setOutlineColor(T entityLivingBaseIn, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);

        Integer color = hook.getOutlineColor(entityLivingBaseIn);
        if (color == null) return;

        GlStateManager.disableLighting();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.color((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F, (color >> 24 & 255) / 255F);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 1f, ordinal = 0))
    public float changeRed(float constant) {

        Color color = getColor();
        return color == null ? constant : color.getRed();
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0f, ordinal = 0))
    public float changeGreen(float constant) {
        Color color = getColor();
        return color == null ? constant : color.getGreen();
    }

    @ModifyConstant(method = "setBrightness", constant = @Constant(floatValue = 0f, ordinal = 1))
    public float changeBlue(float constant) {
        Color color = getColor();
        return color == null ? constant : color.getBlue();
    }

    @Unique
    private Color getColor() {
        return ChangeDamagedEntityColor.getEnabledJVM() ? ChangeDamagedEntityColor.getColorJVM() : null;
    }
}
