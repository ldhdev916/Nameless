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

import com.happyandjust.nameless.devqol.QOLKt;
import com.happyandjust.nameless.features.impl.skyblock.FeatureChangeItemName;
import com.happyandjust.nameless.hypixel.GameType;
import com.happyandjust.nameless.hypixel.Hypixel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private final ItemStack $this = (ItemStack) (Object) this;
    @Shadow
    private NBTTagCompound stackTagCompound;

    @Shadow
    public abstract Item getItem();

    @Inject(method = "getDisplayName", at = @At("HEAD"), cancellable = true)
    public void changeItemName(CallbackInfoReturnable<String> cir) {
        if (Hypixel.INSTANCE.getCurrentGame() != GameType.SKYBLOCK) return;
        FeatureChangeItemName feature = FeatureChangeItemName.INSTANCE;
        if (!feature.getEnabled()) return;

        String skyblockId = QOLKt.getSkyBlockID($this);

        if (feature.isChangedItem(skyblockId)) {
            cir.setReturnValue(feature.changeItemName(skyblockId, getPureDisplayName()));
        }
    }

    private String getPureDisplayName() {
        String s = getItem().getItemStackDisplayName($this);

        if (stackTagCompound != null && this.stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");

            if (nbttagcompound.hasKey("Name", 8)) {
                s = nbttagcompound.getString("Name");
            }
        }

        return s;
    }
}
