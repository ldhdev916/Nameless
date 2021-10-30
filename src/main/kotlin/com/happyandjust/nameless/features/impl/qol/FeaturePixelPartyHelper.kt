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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.toChromaColor
import com.happyandjust.nameless.devqol.getAxisAlignedBB
import com.happyandjust.nameless.devqol.getBlockAtPos
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.features.listener.RenderOverlayListener
import com.happyandjust.nameless.features.listener.WorldRenderListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.block.BlockBeacon
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color

object FeaturePixelPartyHelper : SimpleFeature(Category.QOL, "pixelpartyhelper", "Pixel Party Helper", ""),
    ClientTickListener, WorldRenderListener, RenderOverlayListener {

    init {
        parameters["boxcolor"] = FeatureParameter(
            0,
            "pixelparty",
            "boxcolor",
            "Box Color",
            "",
            Color.red.toChromaColor(),
            CChromaColor
        )
        parameters["beacon"] = FeatureParameter(
            1,
            "pixelparty",
            "beaconcolor",
            "Beacon Color",
            "",
            Color.blue.toChromaColor(),
            CChromaColor
        )
        parameters["arrow"] = FeatureParameter(
            2,
            "pixelparty",
            "beaconarrow",
            "Show Direction Arrow to Beacon",
            "",
            true,
            CBoolean
        )
    }

    private var scanTick = 0
    private val from = BlockPos(-32, 0, 31)
    private val to = BlockPos(31, 0, -32)
    private var sameBlocks = emptySet<AxisAlignedBB>()
    private var beaconPosition: Vec3? = null

    private fun checkForRequirement() = enabled && Hypixel.currentGame == GameType.PIXEL_PARTY

    override fun tick() {
        if (!checkForRequirement()) return
        scanTick = (scanTick + 1) % 15

        if (scanTick != 0) return

        val set = hashSetOf<AxisAlignedBB>()

        mc.thePlayer.inventory.getStackInSlot(8)
            ?.takeIf { it.item == Item.getItemFromBlock(Blocks.stained_hardened_clay) }?.let {
                val meta = it.metadata

                for (pos in BlockPos.getAllInBox(from, to)) {
                    val blockState = mc.theWorld.getBlockState(pos)
                    val block = blockState.block.takeIf { it == Blocks.stained_hardened_clay } ?: continue

                    if (block.getMetaFromState(blockState) == meta) {
                        set.add(pos.getAxisAlignedBB())
                    }
                }
            }
        sameBlocks = set
        beaconPosition = null

        for (posUp in BlockPos.getAllInBox(from.up(), to.up())) {
            if (mc.theWorld.getBlockAtPos(posUp) is BlockBeacon) {
                beaconPosition = Vec3(posUp)
            }
        }
    }

    override fun renderWorld(partialTicks: Float) {
        if (!checkForRequirement()) return

        val boxColor = getParameterValue<Color>("boxcolor").rgb and 0x40FFFFFF

        for (sameBlock in sameBlocks) {
            RenderUtils.drawBox(sameBlock, boxColor, partialTicks)
        }

        beaconPosition?.let {
            RenderUtils.renderBeaconBeam(
                it,
                getParameterValue<Color>("beacon").rgb,
                0.7F,
                partialTicks
            )
        }
    }

    override fun renderOverlay(partialTicks: Float) {
        if (!checkForRequirement()) return

        beaconPosition?.let {
            if (getParameterValue("arrow")) {
                RenderUtils.drawDirectionArrow(it, Color.red.rgb)
            }
        }
    }
}