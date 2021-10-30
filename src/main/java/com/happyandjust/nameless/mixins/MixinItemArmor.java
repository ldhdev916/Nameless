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

import com.happyandjust.nameless.features.FeatureParameter;
import com.happyandjust.nameless.features.FeatureRegistry;
import com.happyandjust.nameless.features.SimpleFeature;
import net.minecraft.init.Items;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(ItemArmor.class)
public class MixinItemArmor {

    private final ItemArmor $this = (ItemArmor) (Object) this;

    private FeatureParameter<Boolean> getAppropriateParameter(SimpleFeature feature) {
        if ($this == Items.leather_helmet) return feature.getParameter("helmet");
        if ($this == Items.leather_chestplate) return feature.getParameter("chestplate");
        if ($this == Items.leather_leggings) return feature.getParameter("leggings");
        if ($this == Items.leather_boots) return feature.getParameter("boots");
        return null;
    }

    private int getCustomColor(SimpleFeature feature) {
        if (!feature.getEnabled()) return Integer.MAX_VALUE;
        FeatureParameter<Boolean> parameter = getAppropriateParameter(feature);
        if (parameter == null || !parameter.getValue()) return Integer.MAX_VALUE;
        return parameter.<Color>getParameterValue("color").getRGB();
    }

    @Inject(method = "hasColor", at = @At("HEAD"), cancellable = true)
    public void injectHasColor(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        SimpleFeature feature = FeatureRegistry.INSTANCE.getCHANGE_LEATHER_ARMOR_COLOR();

        if (feature.getEnabled()) {
            FeatureParameter<Boolean> parameter = getAppropriateParameter(feature);
            if (parameter == null) return;
            if (parameter.getValue()) cir.setReturnValue(true);
        }
    }

    @Inject(method = "getColor", at = @At("HEAD"), cancellable = true)
    public void customizeColor(ItemStack itemStack, CallbackInfoReturnable<Integer> cir) {
        int color = getCustomColor(FeatureRegistry.INSTANCE.getCHANGE_LEATHER_ARMOR_COLOR());

        if (color != Integer.MAX_VALUE) {
            cir.setReturnValue(color);
        }
    }

    @Inject(method = "getColorFromItemStack", at = @At("HEAD"), cancellable = true)
    public void customizeArmorColor2(ItemStack stack, int renderPass, CallbackInfoReturnable<Integer> cir) {
        int color = getCustomColor(FeatureRegistry.INSTANCE.getCHANGE_LEATHER_ARMOR_COLOR());

        if (color != Integer.MAX_VALUE) {
            cir.setReturnValue(color);
        }
    }
}
