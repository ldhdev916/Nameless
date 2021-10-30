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

import com.happyandjust.nameless.features.impl.qol.FeatureAFKMode;
import com.happyandjust.nameless.mixinhooks.RenderGlobalHook;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumWorldBlockLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

    @Unique
    private final RenderGlobalHook hook = RenderGlobalHook.INSTANCE;
    @Shadow
    private WorldClient theWorld;

    @Inject(method = "isRenderEntityOutlines", at = @At("HEAD"), cancellable = true)
    public void renderEntityOutline(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(hook.canDisplayOutline());
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {

        double x = renderViewEntity.prevPosX + (renderViewEntity.posX - renderViewEntity.prevPosX) * partialTicks;
        double y = renderViewEntity.prevPosY + (renderViewEntity.posY - renderViewEntity.prevPosY) * partialTicks;
        double z = renderViewEntity.prevPosZ + (renderViewEntity.posZ - renderViewEntity.prevPosZ) * partialTicks;
        List<Entity> entities = theWorld.loadedEntityList;

        hook.renderOutline(entities, camera, x, y, z, partialTicks);
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z"))
    public boolean cancelOutline(RenderGlobal renderGlobal) {
        return false;
    }


    // AFK

    @Inject(method = "renderEntities", at = @At("HEAD"), cancellable = true)
    public void stopRenderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "setupTerrain", at = @At("HEAD"), cancellable = true)
    public void stopRenderBlocks(Entity viewEntity, double partialTicks, ICamera camera, int frameCount, boolean playerSpectator, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "renderStars", at = @At("HEAD"), cancellable = true)
    public void stopRenderingStar(WorldRenderer worldRendererIn, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "renderClouds", at = @At("HEAD"), cancellable = true)
    public void stopRenderingClouds(float partialTicks, int pass, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "renderSky(FI)V", at = @At("HEAD"), cancellable = true)
    public void stopRenderingSky1(float partialTicks, int pass, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "renderSky(Lnet/minecraft/client/renderer/WorldRenderer;FZ)V", at = @At("HEAD"), cancellable = true)
    public void stopRenderingSky2(WorldRenderer worldRendererIn, float posY, boolean reverseX, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/EnumWorldBlockLayer;DILnet/minecraft/entity/Entity;)I", at = @At("HEAD"), cancellable = true)
    public void stopRenderingBlockLayer1(EnumWorldBlockLayer blockLayerIn, double partialTicks, int pass, Entity entityIn, CallbackInfoReturnable<Integer> cir) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) cir.setReturnValue(0);
    }

    @Inject(method = "renderBlockLayer(Lnet/minecraft/util/EnumWorldBlockLayer;)V", at = @At("HEAD"), cancellable = true)
    public void stopRenderingBlockLayer2(EnumWorldBlockLayer blockLayerIn, CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "generateSky", at = @At("HEAD"), cancellable = true)
    public void stopGeneratingSky(CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "generateSky2", at = @At("HEAD"), cancellable = true)
    public void stopGeneratingSky2(CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }

    @Inject(method = "generateStars", at = @At("HEAD"), cancellable = true)
    public void stopGeneratingStars(CallbackInfo ci) {
        if (FeatureAFKMode.INSTANCE.getEnabled()) ci.cancel();
    }
}
