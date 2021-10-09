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
import com.happyandjust.nameless.features.impl.qol.FeaturePerspective;
import com.happyandjust.nameless.mixinhooks.EntityRendererHook;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderManager.class)
public class MixinRenderManager {

    private final FeaturePerspective feature = FeatureRegistry.INSTANCE.getPERSPECTIVE();
    private final EntityRendererHook hook = EntityRendererHook.INSTANCE;
    @Shadow
    public float playerViewY;
    @Shadow
    public float playerViewX;

    @Inject(method = "cacheActiveRenderInfo", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/renderer/entity/RenderManager;playerViewX:F", shift = At.Shift.AFTER))
    public void modifyYawPitch(World worldIn, FontRenderer textRendererIn, Entity livingPlayerIn, Entity pointedEntityIn, GameSettings optionsIn, float partialTicks, CallbackInfo ci) {
        if (feature.getEnabled()) {
            playerViewY = hook.getCameraYaw();
            playerViewX = hook.getCameraPitch();
        }
    }
}
