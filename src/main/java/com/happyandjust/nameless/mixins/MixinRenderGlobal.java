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

import com.happyandjust.nameless.features.impl.misc.FeatureChangeSkyColor;
import com.happyandjust.nameless.features.impl.qol.FeatureCharm;
import com.happyandjust.nameless.mixinhooks.RenderGlobalHook;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
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

    @Redirect(method = "renderSky(FI)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getSkyColor(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/Vec3;"))
    public Vec3 changeSkyColor(WorldClient instance, Entity entity, float partialTicks) {
        return FeatureChangeSkyColor.INSTANCE.getEnabled() ? FeatureChangeSkyColor.INSTANCE.getConvert() : instance.getSkyColor(entity, partialTicks);
    }

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/RenderManager;renderEntitySimple(Lnet/minecraft/entity/Entity;F)Z", ordinal = 2))
    public boolean cancelRendering(RenderManager instance, Entity entityIn, float partialTicks) {
        if (!(entityIn instanceof EntityPlayer)) return instance.renderEntitySimple(entityIn, partialTicks);
        return !FeatureCharm.INSTANCE.getEnabled() && instance.renderEntitySimple(entityIn, partialTicks);
    }

}
