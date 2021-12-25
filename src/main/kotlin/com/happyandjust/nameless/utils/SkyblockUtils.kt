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
import com.happyandjust.nameless.dsl.notifyException
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants
import java.io.ByteArrayInputStream
import java.util.*

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
                runCatching {
                    it.rarity = ItemRarity.fromString(it.tier_string)
                }.onFailure(Throwable::notifyException)
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