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

import com.happyandjust.nameless.features.impl.general.RemoveNegativeEffects;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityLivingBase.class)
public class MixinEntityLivingBase {

    @Inject(method = "isPotionActive(I)Z", at = @At("HEAD"), cancellable = true)
    public void removeEffects(int potionId, CallbackInfoReturnable<Boolean> cir) {
        if (shouldRemove(potionId)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    public void removeEffects(Potion potionIn, CallbackInfoReturnable<Boolean> cir) {
        if (shouldRemove(potionIn.id)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private boolean shouldRemove(int potionId) {
        if (!RemoveNegativeEffects.getEnabledJVM()) return false;

        List<RemoveNegativeEffects.PotionType> enabledPotionTypes = RemoveNegativeEffects.getEnabledPotionTypesJVM();
        if (potionId == Potion.blindness.id && enabledPotionTypes.contains(RemoveNegativeEffects.PotionType.BLINDNESS)) {
            return true;
        } else
            return potionId == Potion.confusion.id && enabledPotionTypes.contains(RemoveNegativeEffects.PotionType.NAUSEA);
    }
}
