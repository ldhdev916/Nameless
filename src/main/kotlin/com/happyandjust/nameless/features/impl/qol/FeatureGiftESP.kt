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
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import gg.essential.elementa.utils.withAlpha
import kotlinx.coroutines.DelicateCoroutinesApi
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import java.awt.Color

@OptIn(DelicateCoroutinesApi::class)
object FeatureGiftESP : SimpleFeature(Category.QOL, "giftesp", "Gift ESP") {

    private val scanTimer = TickTimer.withSecond(1)
    private val foundGiftsPositions = hashSetOf<BlockPos>()

    private var color by FeatureParameter(
        0,
        "giftesp",
        "color",
        "Box Color",
        "",
        Color.green.withAlpha(64).toChromaColor(),
        CChromaColor
    )

    private var jerryWorkshop by FeatureParameter(
        1,
        "giftesp",
        "jerryworkshop",
        "Enable Jerry Workshop",
        "",
        false,
        CBoolean
    )

    private var lobby by FeatureParameter(1, "giftesp", "lobby", "Enable Hypixel Lobby", "", false, CBoolean)

    private var grinchSimulator by FeatureParameter(
        1,
        "giftesp",
        "grinch",
        "Enable Grinch Simulator",
        "",
        false,
        CBoolean
    )

    private var murderMystery by FeatureParameter(1, "giftesp", "murder", "Enable Murder Mystery", "", false, CBoolean)

    private var currentGiftGameType: GiftGameType? = null

    private val giftArmorStands = hashSetOf<EntityArmorStand>()
    private val giftTileEntities = hashSetOf<TileEntitySkull>()


    init {
        on<SpecialTickEvent>().filter { scanTimer.update().check() }.subscribe {
            currentGiftGameType = if (enabled) GiftGameType.values().find { it.shouldRender() } else null

            giftArmorStands.clear()
            giftTileEntities.clear()

            when (currentGiftGameType) {
                GiftGameType.LOBBY, GiftGameType.GRINCH_SIMULATOR, GiftGameType.MURDER_MYSTERY -> {
                    giftTileEntities.addAll(mc.theWorld.loadedTileEntityList.filterIsInstance<TileEntitySkull>()
                        .filter { check(it.playerProfile?.getSkullOwner()) })

                    giftTileEntities.map { it.pos }
                }
                GiftGameType.JERRY_WORKSHOP -> {
                    giftArmorStands.addAll(mc.theWorld.loadedEntityList.filterIsInstance<EntityArmorStand>().filter {
                        check(it.getEquipmentInSlot(4)?.getSkullOwner())
                    })

                    giftArmorStands.map { BlockPos(it).up(2) }
                }
                else -> return@subscribe
            }
        }

        on<PacketEvent.Sending>().subscribe {
            packet.withInstance<C02PacketUseEntity> {
                val entity = (getEntityFromWorld(mc.theWorld) as? EntityArmorStand)?.takeIf {
                    check(it.getEquipmentInSlot(4)?.getSkullOwner())
                } ?: return@subscribe

                foundGiftsPositions.add(BlockPos(entity).up(2))
                return@subscribe
            }
            packet.withInstance<C08PacketPlayerBlockPlacement> {
                val tileEntitySkull = mc.theWorld.getTileEntity(position) as? TileEntitySkull ?: return@withInstance
                if (check(tileEntitySkull.playerProfile?.getSkullOwner())) {
                    foundGiftsPositions.add(position)
                }

                return@subscribe
            }
        }

        on<HypixelServerChangeEvent>().subscribe {
            foundGiftsPositions.clear()
        }
    }

    private fun check(skullOwner: String?) =
        skullOwner?.getMD5() in (currentGiftGameType?.giftSkullOwners ?: emptyArray())

    @JvmStatic
    fun checkAndRender(tileEntitySkull: TileEntitySkull, partialTicks: Float) {
        if (!enabled) return
        if (tileEntitySkull.pos in foundGiftsPositions) return
        if (tileEntitySkull !in giftTileEntities) return

        render(partialTicks, tileEntitySkull.renderBoundingBox)
    }

    @JvmStatic
    fun checkAndRender(entityLivingBase: EntityLivingBase, partialTicks: Float) {
        if (!enabled) return
        if (entityLivingBase !is EntityArmorStand) return
        if (entityLivingBase !in giftArmorStands) return
        if (BlockPos(entityLivingBase).up(2) in foundGiftsPositions) return

        render(
            partialTicks,
            BlockPos(entityLivingBase).up(2).getAxisAlignedBB()
        )
    }

    private fun render(partialTicks: Float, boundingBox: AxisAlignedBB) {
        RenderUtils.drawBox(boundingBox, color.rgb, partialTicks)
    }

    private fun GiftGameType.shouldRender() = when (this) {
        GiftGameType.JERRY_WORKSHOP -> jerryWorkshop && Hypixel.currentGame == GameType.SKYBLOCK && Hypixel.getProperty<String>(
            PropertyKey.ISLAND
        ) == "winter"
        GiftGameType.LOBBY -> lobby && Hypixel.inLobby
        GiftGameType.GRINCH_SIMULATOR -> grinchSimulator && Hypixel.currentGame == GameType.GRINCH_SIMULATOR
        GiftGameType.MURDER_MYSTERY -> murderMystery && Hypixel.currentGame == GameType.MURDER_MYSTERY
    }


    enum class GiftGameType(vararg val giftSkullOwners: String) {
        JERRY_WORKSHOP("7732c5e41800bb90270f727d2969254b"),
        LOBBY("8ac1ce8ed5f64ed7f878b3ab8a09db0c", "5b18ac9a1e045516d2a442a1e9c1ae60"),
        GRINCH_SIMULATOR(
            "305c6132b239864175877f325d2a6265",
            "33868440c80ee12ec2ce2ff9ac85e754",
            "dd5b404213f4455701f705207b87f994",
            "b20e1b020770d5bc14cc5e65ac67e813",
            "d5f60c1d7af24ecb0d8138bb03e0f6ab",
            "58764113ae6518f46470a780b505935b",
            "3264f0fcf0f1148a6f2dd428d866a097",
            "5b18ac9a1e045516d2a442a1e9c1ae60",
            "e7daab3aad53534c761197741496e521",
            "6fddbd12f8244e2aa337ab3a26c6cfe5",
            "ac296f3bd2b6f3010ab9f02790ab5845",
            "d5eb6a2a3f10ad6b3a6a4d46bb58a5cb",
            "a303d861a92e45e731367d5f7d780252",
            "8ac1ce8ed5f64ed7f878b3ab8a09db0c",
            "780524c8d1554c0daf81a0b2254e63a3",
            "bc74cb052758739553ec70452a983604",
            "d3cd6845a721a7f337f5f124578ce953",
            "fab2cc0520afd7e8bdde670a1f64eada",
            "9d3952daf6c37cfb00199aef51d55719",
            "fc4998737f4a126cc7987d24b57a65d2",
            "61048cd6eb00654d7937ffae0a3c514b",
            "9b31084b64acc821134ce8537f015a0f",
            "2ec2c424a6ad88ccc16c9a0ced5c1303",
            "bf4c144f9866cd4f6bf605e09676ac58",
            "65e5a5f896b000679ed03890f05e2f46"
        ),
        MURDER_MYSTERY(
            "8378fdb4029341cc448362a155d80f92",
            "2e9b0a209a71e784e84ab63185e919b5",
            "95bad214afc3eb1bd234caac6466ea0d"
        );


    }
}