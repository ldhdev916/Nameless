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

import com.happyandjust.nameless.features.FeatureRegistry;
import com.happyandjust.nameless.features.impl.FeatureBedwarsESP;
import com.happyandjust.nameless.features.impl.FeatureGlowStarDungeonMobs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinEntity {

    @Unique
    private final FeatureBedwarsESP bedwarsESP = FeatureRegistry.INSTANCE.getBEDWARS_ESP();
    @Unique
    private final FeatureGlowStarDungeonMobs glowStarDungeonMobs = FeatureRegistry.INSTANCE.getGLOW_STAR_DUNGEON_MOBS();
    private final Entity $this = (Entity) (Object) this;

    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    public void setInvisible(CallbackInfoReturnable<Boolean> cir) {
        if (bedwarsESP.getTeamColorCache().containsKey(this) && bedwarsESP.getEnabled() && (boolean) bedwarsESP.getParameterValue("invisible")) {
            cir.setReturnValue(false);
            return;
        }
        if ($this instanceof EntityEnderman && glowStarDungeonMobs.getEnabled() && glowStarDungeonMobs.getCheckedDungeonMobs().containsValue(this) && (boolean) glowStarDungeonMobs.getParameterValue("fel")) {
            cir.setReturnValue(false);
        }
    }

}
