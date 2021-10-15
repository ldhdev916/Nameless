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
import com.happyandjust.nameless.features.impl.qol.FeatureCancelCertainBlockRendering;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRendererDispatcher.class)
public class MixinBlockRendererDispatcher {

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    public void cancelRender(IBlockState i, BlockPos crashreport, IBlockAccess crashreportcategory, WorldRenderer throwable, CallbackInfoReturnable<Boolean> cir) {
        FeatureCancelCertainBlockRendering feature = FeatureRegistry.INSTANCE.getCANCEL_CERTAIN_BLOCK_RENDERING();

        String key = i.getBlock().getRegistryName();

        if (feature.hasParameter(key) && (boolean) feature.getParameterValue(key)) {
            cir.setReturnValue(false);
        }
    }
}
