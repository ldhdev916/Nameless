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

package com.happyandjust.nameless.features.impl.skyblock

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.hypixel.games.SkyBlock
import com.happyandjust.nameless.hypixel.skyblock.PetSkinType
import com.happyandjust.nameless.utils.SkyblockUtils
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemSkull
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.util.*

@OptIn(ExperimentalSerializationApi::class)
object EquipPetSkin : SimpleFeature(
    "equipPetSkin",
    "Equip Pet Skin",
    "Equip existing pet skin on SkyBlock §ethis only changes 'SKIN'§r, mod gets nearest possible pet and changes its skin so it might be inaccurate"
) {
    private val scanTimer = TickTimer.withSecond(1.5)
    private var currentModifiedPet: EntityArmorStand? = null
        set(value) {
            if (field != value) {
                if (value != null) {

                    field?.let {
                        changePetSkin(it.getPetItem(), PetSkinType.DEFAULT)
                    }

                    val petInfo = pets[value.getPetItem().getSkullOwner().getMD5()]!!
                    val currentSkin = getParameterValue<PetSkinType>(petInfo.petName)
                    changePetSkin(value.getPetItem(), currentSkin)
                }

                field = value
            }
        }

    @JvmField
    var currentPetSkinChangeInfo: PetSkinChangeInfo? = null

    /**
     * key: md5, value: pet name
     */
    private val pets =
        Json.decodeFromStream<Map<String, PetInfo>>(ResourceLocation("nameless", "pets.json").inputStream())

    init {
        val petSkins =
            Json.decodeFromStream<Map<String, String>>(ResourceLocation("nameless", "petskins.json").inputStream())

        val petSkinsByPetName = (PetSkinType.values().toList() - PetSkinType.DEFAULT).groupBy { petSkins[it.name]!! }

        val parameters = petSkinsByPetName.map { (petName, petSkinTypes) ->
            parameter(PetSkinType.DEFAULT) {
                key = petName
                title = petName
                desc = "Pet Skins of $petName"

                settings {
                    serializer { it.prettyName }
                    allValueList = { petSkinTypes + PetSkinType.DEFAULT }
                }

                onValueChange {
                    currentModifiedPet?.let { armorStand ->
                        if (pets[armorStand.getPetItem().getSkullOwner().getMD5()]?.petName == petName) {
                            changePetSkin(armorStand.getPetItem(), it)
                        }
                    }
                }
            }
        }

        hierarchy {
            parameters.forEach { +it }
        }
    }

    private fun changePetSkin(target: ItemStack?, petSkinType: PetSkinType) {
        target ?: return
        val skin = if (petSkinType == PetSkinType.DEFAULT) {
            pets[target.getSkullOwner().getMD5()]?.originSkullOwner
        } else {
            SkyblockUtils.getItemFromId(petSkinType.name)?.skin
        } ?: return
        currentPetSkinChangeInfo = PetSkinChangeInfo(target, GameProfile(UUID.randomUUID(), "ChangePetSkin").apply {
            properties.put("textures", Property("textures", skin, null))
        })
    }

    init {
        on<SpecialTickEvent>().filter {
            enabled && Nameless.hypixel.currentGame is SkyBlock && scanTimer.update().check()
        }.subscribe {
            currentModifiedPet = getNearestPossiblePet()
        }
    }

    private fun getNearestPossiblePet(): EntityArmorStand? {
        return mc.theWorld.getEntitiesWithinAABB(
            EntityArmorStand::class.java,
            mc.thePlayer.entityBoundingBox.expand(10.0, 3.0, 10.0)
        )
            .filter { it.getPetItem()?.item is ItemSkull && it.getPetItem().getSkullOwner().getMD5() in pets }
            .minByOrNull { it.getDistanceSqToEntity(mc.thePlayer) }
    }

    @JvmStatic
    fun checkIfPetIsInInventory(itemStack: ItemStack): GameProfile? {
        if (Nameless.hypixel.currentGame !is SkyBlock) return null
        val gui = mc.currentScreen
        val stacks =
            if (gui is GuiContainer) {
                val container = gui.inventorySlots
                if (container is ContainerChest && container.lowerChestInventory.displayName.unformattedText.stripControlCodes()
                        .startsWith("Auctions")
                ) {
                    mc.thePlayer.inventory.run { armorInventory + mainInventory }.toList()
                } else {
                    gui.inventorySlots.inventory
                }
            } else {
                mc.thePlayer.inventory.run { armorInventory + mainInventory }.toList()
            }

        if (itemStack !in stacks) return null

        val petInfo = pets[itemStack.getSkullOwner().getMD5()] ?: return null

        val petSkinType = getParameterValue<PetSkinType>(petInfo.petName)

        return if (petSkinType == PetSkinType.DEFAULT) {
            null
        } else {
            val skin = SkyblockUtils.getItemFromId(petSkinType.name)?.skin ?: return null
            GameProfile(UUID.randomUUID(), "ChangePetSkinInv").also {
                it.properties.put("textures", Property("textures", skin, null))
            }
        }
    }

    @Serializable
    data class PetInfo(
        @SerialName("name") val petName: String,
        @SerialName("origin") val originSkullOwner: String
    )

    private fun EntityArmorStand.getPetItem() = heldItem ?: getEquipmentInSlot(4)

    data class PetSkinChangeInfo(val itemStack: ItemStack, val gameProfile: GameProfile)
}