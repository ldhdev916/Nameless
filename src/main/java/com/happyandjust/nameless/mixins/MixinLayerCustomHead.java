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

import com.happyandjust.nameless.features.impl.skyblock.FeatureEquipPetSkin;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LayerCustomHead.class)
public class MixinLayerCustomHead {

    @Inject(method = "doRenderLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;renderSkull(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;I)V"), cancellable = true)
    public void changeSkin(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale, CallbackInfo ci) {
        FeatureEquipPetSkin feature = FeatureEquipPetSkin.INSTANCE;
        if (feature.getEnabled()) {
            FeatureEquipPetSkin.PetSkinChangeInfo info = feature.getCurrentPetSkinChangeInfo();

            if (info != null && info.getItemStack().equals(entitylivingbaseIn.getCurrentArmor(3))) {
                TileEntitySkullRenderer.instance.renderSkull(-.5f, 0, -.5f, EnumFacing.UP, 180, 3, info.getGameProfile(), -1);
                GlStateManager.popMatrix();
                ci.cancel();
            }
        }
    }
}
