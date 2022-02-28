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

package com.happyandjust.nameless.utils

import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.hypixel.fairysoul.FairySoul
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockItem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.common.util.Constants
import java.util.*

object SkyblockUtils {
    @OptIn(ExperimentalSerializationApi::class)
    private val fairySoulMap = Json.decodeFromStream<Map<String, List<FairySoul>>>(
        ResourceLocation(
            "nameless",
            "fairysouls.json"
        ).inputStream()
    )
    val allItems by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val items =
            json.decodeFromString<JsonObject>("https://api.hypixel.net/resources/skyblock/items".fetch())["items"]!!

        json.decodeFromJsonElement<List<SkyBlockItem>>(items).associateBy { it.id }
    }

    fun getItemFromId(id: String) = allItems[id.uppercase()]

    fun fetchSkyBlockData() = allItems

    fun getAllFairySoulsByEntity(island: String): List<FairySoul> {
        return mc.theWorld.loadedEntityList.filter { it is EntityArmorStand && it.isFairySoul() }
            .map {
                val pos = BlockPos(it).up(2)
                FairySoul(pos.x, pos.y, pos.z, island)
            }
    }

    fun readNBTFromItemBytes(itemBytes: String): NBTTagCompound {
        val inputStream = Base64.getDecoder().decode(itemBytes).inputStream().buffered()

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
        val jsonObject =
            Json.decodeFromString<JsonObject>("https://api.hypixel.net/skyblock/auctions?page=$page".fetch())

        if (jsonObject["success"]?.boolean != true) return emptyList()

        return Json.decodeFromJsonElement<List<AuctionInfo>>(jsonObject["auctions"]!!).onEach {
            it.skyBlockId = readNBTFromItemBytes(it.item_bytes).getCompoundTag("tag").getCompoundTag("ExtraAttributes")
                .getString("id")
        }
    }

    fun getMaxAuctionPage(): Int {
        val json = Json.decodeFromString<JsonObject>("https://api.hypixel.net/skyblock/auctions".fetch())

        return json["totalPages"]?.int ?: 0
    }
}