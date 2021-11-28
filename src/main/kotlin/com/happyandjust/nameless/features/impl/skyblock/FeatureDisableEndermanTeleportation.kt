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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.EnderTeleportListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import net.minecraft.entity.monster.EntityEnderman
import net.minecraftforge.event.entity.living.EnderTeleportEvent

object FeatureDisableEndermanTeleportation :
    SimpleFeature(Category.SKYBLOCK, "disableendermanteleportation", "Disable Enderman Teleportation in SkyBlock"),
    EnderTeleportListener {


    override fun onEnderTeleport(e: EnderTeleportEvent) {
        if (enabled && Hypixel.currentGame == GameType.SKYBLOCK && e.entity is EntityEnderman) {
            e.isCanceled = true
        }
    }
}