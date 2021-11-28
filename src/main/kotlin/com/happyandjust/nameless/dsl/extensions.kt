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

package com.happyandjust.nameless.dsl

import com.google.gson.JsonObject
import com.happyandjust.nameless.config.ConfigMap
import com.happyandjust.nameless.core.FAIRY_SOUL
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockMonster
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTUtil
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.concurrent.Executors
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

private val RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)")
private val md = MessageDigest.getInstance("MD5")
private val md5Cache = ConfigMap.StringConfigMap("md5")
private val auctionThreadPool = Executors.newFixedThreadPool(6)

/**
 * Taken from SkyblockAddons under MIT License
 *
 * Modified
 *
 * https://github.com/BiscuitDevelopment/SkyblockAddons/blob/master/LICENSE
 * @author Biscuit
 */
fun ItemStack?.getSkyBlockRarity(): ItemRarity? {
    this ?: return null
    if (!hasTagCompound()) return null

    val display = getSubCompound("display", false) ?: return null
    if (!display.hasKey("Lore")) return null
    val lore = display.getTagList("Lore", Constants.NBT.TAG_STRING)

    for (currentLine in (0 until lore.tagCount()).map { lore.getStringTagAt(it) }) {
        RARITY_PATTERN.findMatcher(currentLine) {
            val rarity = it.group("rarity")


            return ItemRarity.values().find { itemRarity -> rarity.startsWith(itemRarity.loreName) }
                ?: return@findMatcher
        }
    }
    return null
}

fun String.getMD5(): String = md5Cache[this] ?: run {
    val bytes = md.digest(toByteArray())

    md5Cache[this] = bytes.joinToString("") { String.format("%02x", it) }
    md5Cache[this]!!
}

fun <T : EntityLivingBase> T.toSkyBlockMonster(): SkyBlockMonster<T>? {
    val identification = SkyblockUtils.getIdentifyArmorStand(this) ?: return null

    val matcher = SkyblockUtils.matchesName(identification, SkyblockUtils.getDefaultPattern())

    val convertHealth: (String) -> Int = {
        when {
            it.endsWith("k") -> it.dropLast(1).toInt() * 1000
            it.endsWith("M") -> it.dropLast(1).toInt() * 100_0000
            else -> it.toInt()
        }
    }

    if (matcher.matches()) {
        return SkyBlockMonster(
            matcher.group("name"),
            matcher.group("level").toInt(),
            convertHealth(matcher.group("current")),
            convertHealth(matcher.group("health")),
            this,
            identification
        )
    }
    return null
}

fun String.copyToClipboard() =
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(this), null)

fun Double.formatDouble(): String =
    DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).also { it.maximumFractionDigits = 640 }
        .format(this)

fun BlockPos.getAxisAlignedBB() =
    AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)


fun EntityArmorStand.isFairySoul(): Boolean {
    if (Hypixel.currentGame != GameType.SKYBLOCK) return false
    return getEquipmentInSlot(4)?.getSkullOwner()?.getMD5() == FAIRY_SOUL
}

fun ItemStack?.getSkyBlockID(): String {
    this ?: return ""
    val tagCompound = getSubCompound("ExtraAttributes", false) ?: return ""

    return tagCompound.getString("id")
}

fun String.stripControlCodes(): String = StringUtils.stripControlCodes(this)

operator fun Vec3.times(m: Double) = Vec3(xCoord * m, yCoord * m, zCoord * m)

operator fun Vec3.div(m: Double) = Vec3(xCoord / m, yCoord / m, zCoord / m)

fun WorldRenderer.pos(x: Int, y: Int, z: Int): WorldRenderer = pos(x.toDouble(), y.toDouble(), z.toDouble())

fun Int.getRedInt() = this shr 16 and 255

fun Int.getGreenInt() = this shr 8 and 255

fun Int.getBlueInt() = this and 255

fun Int.getAlphaInt() = this shr 24 and 255

fun Int.getRedFloat() = getRedInt() / 255f

fun Int.getGreenFloat() = getGreenInt() / 255f

fun Int.getBlueFloat() = getBlueInt() / 255f

fun Int.getAlphaFloat() = getAlphaInt() / 255f

fun Int.pow(n: Int) = toDouble().pow(n).toInt()

fun Int.insertCommaEvery3Character() = toString().reversed().chunked(3).joinToString(",").reversed()

val mc: Minecraft = Minecraft.getMinecraft()

fun World.getBlockAtPos(pos: BlockPos): Block = getBlockState(pos).block

fun BlockPos.toVec3() = Vec3(x.toDouble(), y.toDouble(), z.toDouble())

inline val LOGGER: Logger
    get() = LogManager.getLogger()

fun Throwable.notifyException() {
    sendClientMessage("§cException Occurred ${javaClass.name} $message")
}

fun ItemStack.getSkullOwner(): String {

    return if (hasTagCompound() && tagCompound.hasKey("SkullOwner")) {
        NBTUtil.readGameProfileFromNBT(tagCompound.getCompoundTag("SkullOwner")).properties["textures"].find { it.name == "textures" }?.value
            ?: ""
    } else ""
}

fun scanAuction(task: (List<AuctionInfo>) -> Unit) {
    val allAuctions = arrayListOf<AuctionInfo>()

    val maxPage = SkyblockUtils.getMaxAuctionPage()
    var addedPage = 0

    repeat(maxPage) {
        auctionThreadPool.execute {

            try {
                allAuctions.addAll(SkyblockUtils.getAuctionDataInPage(it))
            } catch (e: Exception) {
                e.notifyException()
            }

            addedPage++

            if (maxPage == addedPage) {
                task(allAuctions)
            }
        }
    }
}

fun EntityPlayerSP?.inHypixel() = this?.clientBrand?.startsWith("Hypixel BungeeCord") == true

inline fun <T> Pattern.matchesMatcher(s: String, block: (Matcher) -> T) =
    matcher(s).takeIf { it.matches() }?.let(block)

inline fun <T> Pattern.findMatcher(s: String, block: (Matcher) -> T) = matcher(s).takeIf { it.find() }?.let(block)

fun Entity.toVec3() = Vec3(posX, posY, posZ)

fun Double.transformToPrecision(precision: Int): Double {
    if (precision == 0) return roundToInt().toDouble()

    val pow = 10.pow(precision)

    return round(this * pow) / pow
}

fun Double.transformToPrecisionString(precision: Int) = transformToPrecision(precision).formatDouble()

fun String.decodeBase64() = Base64.getDecoder().decode(this).decodeToString()

inline fun <T> nullCatch(defaultValue: T, block: () -> T) = try {
    block()
} catch (e: NullPointerException) {
    defaultValue
}

operator fun JsonObject.iterator() = entrySet().iterator()