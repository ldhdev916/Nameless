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

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.devqol.getMD5
import com.happyandjust.nameless.devqol.getSkullOwner
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.stripControlCodes
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.ClientTickListener
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.PetSkinType
import com.happyandjust.nameless.serialization.converters.CPetSkinType
import com.happyandjust.nameless.utils.SkyblockUtils
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemSkull
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import java.util.*

object FeatureEquipPetSkin : SimpleFeature(
    Category.SKYBLOCK,
    "equippetskin",
    "Equip Pet Skin",
    "Equip existing pet skin on SkyBlock §ethis only changes 'SKIN'§r, mod gets nearest possible pet and changes its skin so it might be inaccurate"
), ClientTickListener {

    /**
     * key: md5, value: pet name
     */
    private var pets = mapOf<String, PetInfo>()
    private var scanTick = 0
    private var currentModifiedPet: EntityArmorStand? = null
        set(value) {
            if (field != value) {
                if (value != null) {

                    field?.let {
                        changePetSkin(it.getPetItem(), PetSkinType.DEFAULT)
                    }

                    val petInfo = pets[value.getPetItem().getSkullOwner().getMD5()]!!
                    val currentSkin = getParameterValue<PetSkinType>(petInfo.petName.lowercase())
                    changePetSkin(value.getPetItem(), currentSkin)
                }

                field = value
            }
        }
    private val gson = Gson()
    var currentPetSkinChangeInfo: PetSkinChangeInfo? = null

    fun fetchPetSkinData() {
        pets = hashMapOf(
            *JSONHandler(ResourceLocation("nameless", "pets.json")).read(JsonObject()).entrySet()
                .map { it.key to gson.fromJson(it.value, PetInfo::class.java) }.toTypedArray()
        )

        val petSkins = hashMapOf(
            *JSONHandler(ResourceLocation("nameless", "petskins.json")).read(JsonObject()).entrySet()
                .map { it.key to it.value.asString }.toTypedArray()
        )

        val petSkinsByPetName = (PetSkinType.values().toList() - PetSkinType.DEFAULT).groupBy { petSkins[it.name]!! }

        for ((petName, petSkinTypes) in petSkinsByPetName) {
            parameters[petName.lowercase()] = FeatureParameter(
                0,
                "equippetskin",
                petName.lowercase(),
                petName,
                "Pet Skins of $petName",
                PetSkinType.DEFAULT,
                CPetSkinType
            ).also {
                it.allEnumList = petSkinTypes + PetSkinType.DEFAULT
                it.enumName = { enum ->
                    (enum as PetSkinType).prettyName
                }

                it.onValueChange = { petSkinType ->
                    currentModifiedPet?.let { armorStand ->
                        if (pets[armorStand.getPetItem().getSkullOwner().getMD5()]!!.petName == petName) {
                            changePetSkin(armorStand.getPetItem(), petSkinType)
                        }
                    }
                }
            }
        }
    }

    private fun changePetSkin(target: ItemStack?, petSkinType: PetSkinType) {
        target ?: return
        val skin = if (petSkinType == PetSkinType.DEFAULT) {
            pets[target.getSkullOwner().getMD5()]?.originSkullOwner
        } else {
            SkyblockUtils.getItemFromId(petSkinType.name)?.skin
        } ?: return
        currentPetSkinChangeInfo = PetSkinChangeInfo(target, GameProfile(UUID.randomUUID(), "ChangePetSkin").also {
            it.properties.put("textures", Property("textures", skin, null))
        })
    }

    override fun tick() {
        if (enabled && Hypixel.currentGame == GameType.SKYBLOCK) {
            scanTick = (scanTick + 1) % 30

            if (scanTick == 0) {
                currentModifiedPet = getNearestPossiblePet()
            }
        }
    }

    private fun getNearestPossiblePet(): EntityArmorStand? {
        return mc.theWorld.getEntitiesWithinAABB(
            EntityArmorStand::class.java,
            mc.thePlayer.entityBoundingBox.expand(10.0, 3.0, 10.0)
        )
            .asSequence()
            .filter { it.getPetItem()?.item is ItemSkull }
            .filter { pets.contains(it.getPetItem().getSkullOwner().getMD5()) }
            .minByOrNull { it.getDistanceSqToEntity(mc.thePlayer) }
    }

    fun checkIfPetIsInInventory(itemStack: ItemStack): GameProfile? {
        if (Hypixel.currentGame != GameType.SKYBLOCK) return null
        val gui = mc.currentScreen
        val stacks =
            if (gui is GuiContainer) {
                val container = gui.inventorySlots
                if (container is ContainerChest && container.lowerChestInventory.displayName.unformattedText.stripControlCodes()
                        .startsWith("Auctions")
                ) {
                    mc.thePlayer.inventory.let { it.armorInventory + it.mainInventory }.toList()
                } else {
                    gui.inventorySlots.inventory
                }
            } else {
                mc.thePlayer.inventory.let { it.armorInventory + it.mainInventory }.toList()
            }

        if (!stacks.contains(itemStack)) return null

        val petInfo = pets[itemStack.getSkullOwner().getMD5()] ?: return null

        val petSkinType = getParameterValue<PetSkinType>(petInfo.petName.lowercase())

        return if (petSkinType == PetSkinType.DEFAULT) {
            null
        } else {
            val skin = SkyblockUtils.getItemFromId(petSkinType.name)?.skin ?: return null
            GameProfile(UUID.randomUUID(), "ChangePetSkinInv").also {
                it.properties.put("textures", Property("textures", skin, null))
            }
        }
    }

    class PetInfo {
        @SerializedName("name")
        var petName = ""

        @SerializedName("origin")
        var originSkullOwner = ""
    }

    private fun EntityArmorStand.getPetItem() = heldItem ?: getEquipmentInSlot(4)

    data class PetSkinChangeInfo(val itemStack: ItemStack, val gameProfile: GameProfile)
}