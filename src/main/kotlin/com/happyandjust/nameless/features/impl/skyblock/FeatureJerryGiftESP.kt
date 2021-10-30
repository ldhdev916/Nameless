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

import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.getAxisAlignedBB
import com.happyandjust.nameless.devqol.getMD5
import com.happyandjust.nameless.devqol.getSkullOwner
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.PacketListener
import com.happyandjust.nameless.features.listener.ServerChangeListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.util.BlockPos
import java.awt.Color

object FeatureJerryGiftESP : SimpleFeature(Category.SKYBLOCK, "jerrygiftesp", "Jerry Workshop Gift ESP"),
    ClientTickListener, WorldRenderListener, ServerChangeListener, PacketListener {

    private var gifts = hashSetOf<EntityArmorStand>()
    private val foundGifts = hashSetOf<EntityArmorStand>()
    private var scanTick = 0


    init {
        parameters["color"] = FeatureParameter(
            0,
            "jerrygiftesp",
            "color",
            "Box Color",
            "",
            Color.green.toChromaColor(),
            CChromaColor
        )
    }

    private fun checkForRequirements() =
        enabled && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty<String>(PropertyKey.ISLAND) == "winter"

    override fun tick() {
        if (!checkForRequirements()) return
        scanTick = (scanTick + 1) % 20

        if (scanTick != 0) return

        val giftList = hashSetOf<EntityArmorStand>()
        for (entityArmorStand in mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>()) {
            val head = entityArmorStand.getEquipmentInSlot(4) ?: continue

            if (head.getSkullOwner().getMD5() == "7732c5e41800bb90270f727d2969254b") {
                giftList.add(entityArmorStand)
            }
        }

        gifts = giftList
    }

    override fun renderWorld(partialTicks: Float) {
        if (!checkForRequirements()) return

        val color = getParameterValue<Color>("color").rgb and 0x40FFFFFF

        for (gift in gifts.filter { !foundGifts.contains(it) }) {
            RenderUtils.drawBox(BlockPos(gift).up(2).getAxisAlignedBB(), color, partialTicks)
        }
    }

    override fun onSendingPacket(e: PacketEvent.Sending) {
        val msg = e.packet

        if (msg is C02PacketUseEntity) {
            val entity = msg.getEntityFromWorld(mc.theWorld) as? EntityArmorStand ?: return

            if (gifts.contains(entity)) foundGifts.add(entity)
        }
    }

    override fun onReceivedPacket(e: PacketEvent.Received) {

    }

    override fun onServerChange(server: String) {
        foundGifts.clear()
        gifts = hashSetOf()
    }

}