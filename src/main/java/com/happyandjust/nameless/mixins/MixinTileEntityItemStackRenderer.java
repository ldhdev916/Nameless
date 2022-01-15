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

import com.happyandjust.nameless.features.impl.skyblock.ChangeHelmetTexture;
import com.happyandjust.nameless.features.impl.skyblock.EquipPetSkin;
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem;
import com.mojang.authlib.GameProfile;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TileEntityItemStackRenderer.class)
public class MixinTileEntityItemStackRenderer {

    @Inject(method = "renderByItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntitySkullRenderer;renderSkull(FFFLnet/minecraft/util/EnumFacing;FILcom/mojang/authlib/GameProfile;I)V"), cancellable = true)
    public void changeSkin(ItemStack itemStack, CallbackInfo ci) {
        EquipPetSkin feature = EquipPetSkin.INSTANCE;
        ChangeHelmetTexture featureChangeHelmetTexture = ChangeHelmetTexture.INSTANCE;
        GameProfile gameProfile = null;
        if (feature.getEnabled()) {

            EquipPetSkin.PetSkinChangeInfo info = feature.getCurrentPetSkinChangeInfo();
            if (info != null && info.getItemStack().equals(itemStack)) {
                gameProfile = info.getGameProfile();
            }
            if (gameProfile == null) {
                gameProfile = feature.checkIfPetIsInInventory(itemStack);
            }
        }

        if (featureChangeHelmetTexture.getEnabled() && itemStack.equals(Minecraft.getMinecraft().thePlayer.getEquipmentInSlot(4))) {
            Pair<SkyBlockItem, GameProfile> pair = featureChangeHelmetTexture.getCurrentlyEquipedTexture();

            if (pair != null) gameProfile = pair.getSecond();
        }

        if (gameProfile != null) {
            TileEntitySkullRenderer.instance.renderSkull(0, 0, 0, EnumFacing.UP, 0, 3, gameProfile, -1);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
            ci.cancel();
        }
    }
}
