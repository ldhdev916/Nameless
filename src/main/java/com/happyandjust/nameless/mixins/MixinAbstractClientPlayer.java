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

import com.happyandjust.nameless.features.impl.misc.FeatureDisguiseNickname;
import com.happyandjust.nameless.resourcepack.SkinResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public class MixinAbstractClientPlayer {

    private final AbstractClientPlayer $this = (AbstractClientPlayer) (Object) this;

    @Inject(method = "getLocationSkin()Lnet/minecraft/util/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    public void changeSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        if ($this != Minecraft.getMinecraft().thePlayer) return;
        FeatureDisguiseNickname feature = FeatureDisguiseNickname.INSTANCE;
        if (!feature.getEnabled()) return;
        if (!feature.<Boolean>getParameterValue("skin")) return;

        String nickname = feature.getNickname();

        ResourceLocation resourceLocation = feature.getCachedUsernameResourceLocation().get(nickname);
        if (resourceLocation != null && SkinResourcePack.INSTANCE.resourceExists(resourceLocation)) {
            cir.setReturnValue(resourceLocation);
            return;
        }

        feature.checkAndDownloadSkin(nickname);

    }
}
