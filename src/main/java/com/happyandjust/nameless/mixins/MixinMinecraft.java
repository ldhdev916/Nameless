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

import com.happyandjust.nameless.features.impl.misc.FeatureHitDelayFix;
import com.happyandjust.nameless.features.impl.qol.FeaturePerspective;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Shadow
    private int leftClickCounter;

    @Inject(method = "clickMouse", at = @At("HEAD"))
    public void onClickMouse(CallbackInfo ci) {
        if (FeatureHitDelayFix.INSTANCE.getEnabled()) {
            leftClickCounter = 0;
        }
    }

    @Redirect(method = "runTick", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD, target = "Lnet/minecraft/client/settings/GameSettings;thirdPersonView:I"))
    public void blockPerspective(GameSettings gameSettings, int value) {
        gameSettings.thirdPersonView = FeaturePerspective.INSTANCE.getEnabled() ? 1 : value;
    }
}
