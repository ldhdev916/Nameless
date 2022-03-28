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

package com.happyandjust.nameless.mixins.sba;

import com.happyandjust.nameless.features.impl.settings.DisableSBAGlowing;
import net.minecraft.client.renderer.culling.ICamera;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.features.EntityOutlines.EntityOutlineRenderer", remap = false)
public class MixinEntityOutlineRenderer {

    @Dynamic
    @Inject(method = "renderEntityOutlines", at = @At("RETURN"), cancellable = true)
    private static void letMeDrawOutline(ICamera camera, float partialTicks, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (DisableSBAGlowing.INSTANCE.getEnabled()) {
            cir.setReturnValue(true);
        }
    }
}
