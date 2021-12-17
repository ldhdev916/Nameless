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

import com.happyandjust.nameless.core.JERRY_GIFT
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.elementa.utils.withAlpha
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

object FeatureJerryGiftESP : SimpleFeature(Category.SKYBLOCK, "jerrygiftesp", "Jerry Workshop Gift ESP") {

    private val gifts = hashSetOf<EntityArmorStand>()
    private val foundGifts = hashSetOf<EntityArmorStand>()
    private val scanTimer = TickTimer.withSecond(1)
    private var color by FeatureParameter(
        0,
        "jerrygiftesp",
        "color",
        "Box Color",
        "",
        Color.green.withAlpha(64).toChromaColor(),
        CChromaColor
    )

    private fun checkForRequirements() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty<String>(PropertyKey.ISLAND) == "winter"

    init {
        on<SpecialTickEvent>().filter { checkForRequirements() && scanTimer.update().check() }.subscribe {
            gifts.clear()
            gifts.addAll(mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()
                .filter { it.getEquipmentInSlot(4)?.getSkullOwner()?.getMD5() == JERRY_GIFT })
        }

        on<RenderWorldLastEvent>().filter { checkForRequirements() }.subscribe {
            for (gift in gifts - foundGifts) {
                RenderUtils.drawBox(BlockPos(gift).up(2).getAxisAlignedBB(), color.rgb, partialTicks)
            }
        }

        on<PacketEvent.Sending>().subscribe {
            packet.withInstance<C02PacketUseEntity> {
                val entity = getEntityFromWorld(mc.theWorld) as? EntityArmorStand ?: return@subscribe

                if (entity in gifts) foundGifts.add(entity)
            }
        }

        on<HypixelServerChangeEvent>().subscribe {
            foundGifts.clear()
            gifts.clear()
        }
    }

}