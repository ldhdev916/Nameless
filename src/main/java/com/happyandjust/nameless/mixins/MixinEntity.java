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

import com.happyandjust.nameless.features.impl.general.FeatureBedwarsESP;
import com.happyandjust.nameless.features.impl.general.FeatureGlowAllPlayers;
import com.happyandjust.nameless.features.impl.skyblock.FeatureGlowStarDungeonMobs;
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
        FeatureBedwarsESP bedwarsESP = FeatureBedwarsESP.INSTANCE;
        FeatureGlowStarDungeonMobs glowStarDungeonMobs = FeatureGlowStarDungeonMobs.INSTANCE;
        FeatureGlowAllPlayers glowAllPlayers = FeatureGlowAllPlayers.INSTANCE;
        if (bedwarsESP.getTeamColorCache().containsKey(this) && bedwarsESP.getEnabled() && (boolean) bedwarsESP.getParameterValue("invisible")) {
            cir.setReturnValue(false);
            return;
        }
        if ($this instanceof EntityEnderman && glowStarDungeonMobs.getEnabled() && glowStarDungeonMobs.getCheckedDungeonMobs().containsValue(this) && (boolean) glowStarDungeonMobs.getParameterValue("fel")) {
            cir.setReturnValue(false);
        }
        if (glowAllPlayers.getEnabled() && glowAllPlayers.<Boolean>getParameterValue("invisible") && glowAllPlayers.getPlayersInTab().contains(this) && $this instanceof EntityPlayer) {
            if (getFlag(5)) { // invisible
                glowAllPlayers.getInvisiblePlayers().add((EntityPlayer) $this);
            }
            cir.setReturnValue(false);
        }
    }

    @ModifyVariable(method = "getDisplayName", at = @At(value = "STORE"))
    public ChatComponentText getCustomDamageName(ChatComponentText origin) {
        if (!($this instanceof EntityArmorStand)) return origin;
        return hook.getCustomDamageName(origin);
    }

}
