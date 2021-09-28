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
import com.happyandjust.nameless.features.impl.FeaturePerspective;
import com.happyandjust.nameless.mixinhooks.EntityRendererHook;
import net.minecraft.client.renderer.ActiveRenderInfo;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ActiveRenderInfo.class)
public class MixinActiveRenderInfo {

    private static final FeaturePerspective feature = FeatureRegistry.INSTANCE.getPERSPECTIVE();
    private static final EntityRendererHook hook = EntityRendererHook.INSTANCE;

    @ModifyVariable(method = "updateRenderInfo", at = @At(value = "STORE", opcode = Opcodes.FSTORE), ordinal = 2)
    private static float modifyPitch(float value) {
        return feature.getEnabled() ? hook.getCameraPitch() : value;
    }

    @ModifyVariable(method = "updateRenderInfo", at = @At(value = "STORE", opcode = Opcodes.FSTORE), ordinal = 3)
    private static float modifyYaw(float value) {
        return feature.getEnabled() ? hook.getCameraYaw() : value;
    }
}
