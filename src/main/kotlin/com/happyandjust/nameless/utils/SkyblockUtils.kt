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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JSONHandler
import com.happyandjust.nameless.devqol.isFairySoul
import com.happyandjust.nameless.devqol.mc
import com.happyandjust.nameless.devqol.stripControlCodes
import com.happyandjust.nameless.hypixel.auction.AuctionInfo
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import com.happyandjust.nameless.network.Request
import com.happyandjust.nameless.serialization.TypeRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow

object SkyblockUtils {
    private val fairySoulMap = hashMapOf<String, List<FairySoul>>()
    private val gson = Gson()
    val allItems = hashMapOf<String, SkyBlockItem>().also {
        val s = Request.get("https://api.hypixel.net/resources/skyblock/items")

        val json = JSONHandler(s).read(JsonObject())

        val items = json["items"].asJsonArray

        for (item in items) {
            val skyBlockItem = gson.fromJson(item, SkyBlockItem::class.java)

            skyBlockItem.rarity = ItemRarity.fromString(skyBlockItem.stringRarity)
            it[skyBlockItem.id] = skyBlockItem
        }
    }

    fun getItemFromId(id: String) = allItems[id.uppercase()]!!

    fun fetchSkyBlockData() {
        val handler = JSONHandler(ResourceLocation("nameless", "fairysouls.json"))

        val souls = handler.read(JsonObject())
        val fairySoulDeserializer = TypeRegistry.getConverterByClass(FairySoul::class)

        for ((island, fairySouls) in souls.entrySet()) {
            if (fairySouls is JsonArray) {
                val list = arrayListOf<FairySoul>()

                for (coord in fairySouls) {
                    if (coord is JsonObject) {
                        // in json they don't have island property
                        list.add(fairySoulDeserializer.deserialize(coord.apply { addProperty("island", island) }))
                    }
                }
                fairySoulMap[island] = list
            }
        }
    }

    fun getAllFairySoulsByEntity(island: String): List<FairySoul> {
        val fairySouls = mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.isFairySoul() }

        return arrayListOf<FairySoul>().apply {
            for (fairySoul in fairySouls) {
                val pos = BlockPos(fairySoul).add(0, 2, 0)
                add(FairySoul(pos.x, pos.y, pos.z, island))
            }
        }
    }

    fun getMobNamePattern(level: Int, name: String): Pattern =
        Pattern.compile("\\[Lv$level] (Runic )?$name (?<current>\\d+)/(?<health>\\d+).")

    fun getDefaultPattern(): Pattern =
        Pattern.compile("\\[Lv(?<level>\\d+)] (Runic )?(?<name>(\\w|\\s)+) (?<current>\\d+)/(?<health>\\d+).")

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

        val json = JSONHandler(s).read(JsonObject())

        if (!json["success"].asBoolean) return emptyList()

        val list = arrayListOf<AuctionInfo>()

        val auctions = json["auctions"].asJsonArray

        for (auction in auctions) {
            list.add(gson.fromJson(auction, AuctionInfo::class.java))
        }

        return list
    }
}
