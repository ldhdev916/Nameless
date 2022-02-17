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

import com.happyandjust.nameless.features.impl.misc.ChangeLeatherArmorColor;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemArmor.class)
public class MixinItemArmor {

    private final ItemArmor $this = (ItemArmor) (Object) this;

    @Inject(method = "hasColor", at = @At("HEAD"), cancellable = true)
    public void injectHasColor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (ChangeLeatherArmorColor.getCustomColor($this, stack) != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    public void customizeColor(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        Integer color = ChangeLeatherArmorColor.getCustomColor($this, itemStack);

        if (color != null) {
            cir.setReturnValue(color);
        }
    }

    @Inject(method = "getColorFromItemStack", at = @At("HEAD"), cancellable = true)
    public void customizeArmorColor2(ItemStack stack, int renderPass, CallbackInfoReturnable<Integer> cir) {
        Integer color = ChangeLeatherArmorColor.getCustomColor($this, stack);

        if (color != null) {
            cir.setReturnValue(color);
        }
    }
}
