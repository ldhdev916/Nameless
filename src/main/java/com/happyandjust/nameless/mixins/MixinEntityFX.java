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

import com.happyandjust.nameless.features.impl.misc.ChangeFishParticleColor;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.particle.EntityFishWakeFX;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityFX.class)
public class MixinEntityFX {

    private final EntityFX $this = (EntityFX) (Object) this;
    @Shadow
    protected float particleRed;
    @Shadow
    protected float particleGreen;
    @Shadow
    protected float particleBlue;
    @Shadow
    protected float particleAlpha;

    @Inject(method = "renderParticle", at = @At("HEAD"))
    public void setColor(WorldRenderer worldRendererIn, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ, CallbackInfo ci) {
        if (!($this instanceof EntityFishWakeFX)) return;

        if (ChangeFishParticleColor.INSTANCE.getEnabled()) {
            int color = ChangeFishParticleColor.getColor().getRGB();

            particleRed = (color >> 16 & 255) / 255F;
            particleGreen = (color >> 8 & 255) / 255F;
            particleBlue = (color & 255) / 255F;
            particleAlpha = (color >> 24 & 255) / 255F;
        }
    }
}
