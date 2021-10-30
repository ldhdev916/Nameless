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

import com.google.common.collect.Collections2;
import com.happyandjust.nameless.features.impl.qol.FeatureHideNPC;
import com.happyandjust.nameless.mixinhooks.GuiPlayerTabOverlayHook;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(GuiPlayerTabOverlay.class)
public class MixinGuiPlayerTabOverlay {

    private final GuiPlayerTabOverlayHook hook = GuiPlayerTabOverlayHook.INSTANCE;

    @Inject(method = "drawPing", at = @At("HEAD"), cancellable = true)
    public void drawCustomPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn, CallbackInfo ci) {
        hook.drawCustomPing(p_175245_1_, p_175245_2_, p_175245_3_, networkPlayerInfoIn, ci);
    }

    @Redirect(method = "renderPlayerlist", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/NetHandlerPlayClient;getPlayerInfoMap()Ljava/util/Collection;"))
    public Collection<NetworkPlayerInfo> filterNPC(NetHandlerPlayClient netHandlerPlayClient) {
        Collection<NetworkPlayerInfo> map = netHandlerPlayClient.getPlayerInfoMap();

        return FeatureHideNPC.INSTANCE.getEnabled() ? Collections2.filter(map, player -> player != null && player.getGameProfile().getId().version() != 2) : map;
    }
}
