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

import com.happyandjust.nameless.features.impl.general.BedWarsESP;
import com.happyandjust.nameless.features.impl.general.GlowAllPlayers;
import com.happyandjust.nameless.features.impl.skyblock.GlowStarDungeonMobs;
import com.happyandjust.nameless.mixinhooks.EntityHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

    @Unique
    private final EntityHook hook = EntityHook.INSTANCE;
    private final Entity $this = (Entity) (Object) this;

    @Shadow
    protected abstract boolean getFlag(int flag);

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    public void setInvisible(CallbackInfoReturnable<Boolean> cir) {
        if (BedWarsESP.teamColorCache.containsKey(this) && BedWarsESP.getEnabledJVM() && BedWarsESP.getInvisibleJVM()) {
            cir.setReturnValue(false);
            return;
        }
        if ($this instanceof EntityEnderman && GlowStarDungeonMobs.getEnabledJVM() && GlowStarDungeonMobs.checkedDungeonMobs.containsValue(this) && GlowStarDungeonMobs.getShowFelJVM()) {
            cir.setReturnValue(false);
        }
        if (GlowAllPlayers.getEnabledJVM() && GlowAllPlayers.getInvisibleJVM() && GlowAllPlayers.playersInTab.contains(this) && $this instanceof EntityPlayer) {
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "getDisplayName", at = @At(value = "STORE"))
    public ChatComponentText getCustomDamageName(ChatComponentText origin) {
        if (!($this instanceof EntityArmorStand)) return origin;
        return hook.getCustomDamageName(origin);
    }

}
