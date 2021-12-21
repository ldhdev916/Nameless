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

package com.happyandjust.nameless.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.core.Request
import com.happyandjust.nameless.dsl.isFairySoul
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.dsl.stripControlCodes
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants
import java.io.ByteArrayInputStream
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow

object SkyblockUtils {
    private val gson = Gson()
    private val fairySoulMap by lazy {
        JsonHandler(ResourceLocation("nameless", "fairysouls.json")).read(JsonObject())
            .entrySet()
            .associate { (key, value) ->
                key to value.asJsonArray.map {
                    val jsonObject = it.asJsonObject
                    FairySoul(jsonObject["x"].asInt, jsonObject["y"].asInt, jsonObject["z"].asInt, key)
                }
            }
    }
    val allItems by lazy {
        JsonHandler(Request.get("https://api.hypixel.net/resources/skyblock/items")).read(JsonObject())["items"].asJsonArray.associate {
            val item = gson.fromJson(it, SkyBlockItem::class.java)

            item.id to item
        }
    }

    fun getItemFromId(id: String) = allItems[id.uppercase()]

    fun fetchSkyBlockData() {
        fairySoulMap
        allItems
    }

    fun getAllFairySoulsByEntity(island: String): List<FairySoul> {
        return mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.isFairySoul() }
            .map {
                val pos = BlockPos(it).up(2)
                FairySoul(pos.x, pos.y, pos.z, island)
            }
    }

    fun readNBTFromItemBytes(itemBytes: String): NBTTagCompound {
        val inputStream = ByteArrayInputStream(Base64.getDecoder().decode(itemBytes)).buffered()

        val nbt = CompressedStreamTools.readCompressed(inputStream)

        return nbt.getTagList("i", Constants.NBT.TAG_COMPOUND).getCompoundTagAt(0)
    }

    fun getMobNamePattern(level: Int, name: String): Pattern =
        Pattern.compile("\\[Lv$level] (Runic )?$name (?<current>\\d+)/(?<health>\\d+).")

    fun getDefaultPattern(): Pattern =
        Pattern.compile("\\[Lv(?<level>\\d+)] (Runic )?(?<name>(\\w|\\s)+) (?<current>(\\d|k|M)+)/(?<health>(\\d|k|M)+).")

    fun matchesName(entityArmorStand: EntityArmorStand, pattern: Pattern): Matcher =
        pattern.matcher(entityArmorStand.displayName.unformattedText.stripControlCodes())

    fun getIdentifyArmorStand(entity: Entity): EntityArmorStand? {
        if (entity is EntityArmorStand) {
            if (matchesName(entity, getDefaultPattern()).matches()) {
                return entity
            }
        }

        val aabb = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(aabb.minX, aabb.maxY, aabb.minZ, aabb.maxX, aabb.maxY + 1, aabb.maxZ)

        val list = entity.worldObj.getEntitiesWithinAABB(EntityArmorStand::class.java, axisAlignedBB)

        list.removeIf { !matchesName(it, getDefaultPattern()).matches() }

        if (list.size > 0) {
            list.sortBy { (it.posX - entity.posX).pow(2) + (it.posZ - entity.posZ).pow(2) }

            return list[0]
        }

        return null
    }

    fun getAllFairySoulsInWorld(island: String): List<FairySoul> {
        if (island == "dungeon") {
            // get all fairysouls by entity
            return getAllFairySoulsByEntity("dungeon")
        }

        return fairySoulMap[island] ?: return emptyList()
    }

    fun getAuctionDataInPage(page: Int): List<AuctionInfo> {
        val s = Request.get("https://api.hypixel.net/skyblock/auctions?page=$page")

        val json = JsonHandler(s).read(JsonObject())

        if (!json["success"].asBoolean) return emptyList()

        val list = arrayListOf<AuctionInfo>()

        val auctions = json["auctions"].asJsonArray

        for (auction in auctions) {
            list.add(gson.fromJson(auction, AuctionInfo::class.java).also {
                try {
                    it.rarity = ItemRarity.fromString(it.tier_string)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                it.skyBlockId =
                    readNBTFromItemBytes(it.item_bytes).getCompoundTag("tag").getCompoundTag("ExtraAttributes")
                        .getString("id")
            })
        }

        return list
    }

    fun getMaxAuctionPage(): Int {
        val s = Request.get("https://api.hypixel.net/skyblock/auctions")

        val json = JsonHandler(s).read(JsonObject())

        if (!json["success"].asBoolean) return 0

        return json["totalPages"].asInt
    }
}