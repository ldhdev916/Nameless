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

package com.happyandjust.nameless.devqol

import com.happyandjust.nameless.config.ConfigMap
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.auction.AuctionInfo
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.happyandjust.nameless.hypixel.skyblock.SkyBlockMonster
import com.happyandjust.nameless.mixins.accessors.AccessorNBTTagList
import com.happyandjust.nameless.utils.SkyblockUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
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
private val hsbCache = hashMapOf<Int, FloatArray>()
val md = MessageDigest.getInstance("MD5")
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

    for (i in 0 until lore.tagCount()) {
        val currentLine = lore.getStringTagAt(i)

        val matcher = RARITY_PATTERN.matcher(currentLine)
        if (matcher.find()) {
            val rarity = matcher.group("rarity")

            for (itemRarity in ItemRarity.values()) {
                return if (rarity.startsWith(itemRarity.loreName)) itemRarity else continue
            }
        }
    }
    return null
}

fun String.getMD5(): String = md5Cache[this] ?: run {
    val bytes = md.also { it.update(toByteArray(StandardCharsets.UTF_8)) }.digest()

    val builder = StringBuilder()

    for (byte in bytes) {
        builder.append(String.format("%02x", byte))
    }

    builder.toString().also {
        md5Cache[this] = it
    }
}

fun <T : EntityLivingBase> T.toSkyBlockMonster(): SkyBlockMonster<T>? {
    val identification = SkyblockUtils.getIdentifyArmorStand(this) ?: return null

    val matcher = SkyblockUtils.matchesName(identification, SkyblockUtils.getDefaultPattern())

    if (matcher.matches()) {
        return SkyBlockMonster(
            matcher.group("name"),
            matcher.group("level").toInt(),
            matcher.group("current").toInt(),
            matcher.group("health").toInt(),
            this,
            identification
        )
    }

    return null
}

fun String.copyToClipboard() =
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(this), null)

fun EntityPlayerSP.setYawPitch(yaw: Float, pitch: Float) = setPositionAndRotation(posX, posY, posZ, yaw, pitch)

fun BlockPos.getSurroundings(): List<BlockPos> {
    val list = arrayListOf<BlockPos>()

    for (x in -1..1) {
        for (z in -1..1) {
            list.add(add(x, 0, z).takeUnless { it == this } ?: continue)
        }
    }

    return list
}

fun Double.formatDouble() =
    DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).also { it.maximumFractionDigits = 640 }
        .format(this)

fun Random.getBetween(start: Int, end: Int) = nextInt(end - start + 1) + start

fun BlockPos.getAxisAlignedBB() =
    AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)


fun EntityArmorStand.isFairySoul(): Boolean {
    if (Hypixel.currentGame != GameType.SKYBLOCK) return false

    val itemStack = getEquipmentInSlot(4) ?: return false
    return itemStack.getSkullOwner().getMD5() == "57a4c8dc9b8e5d4180daa608901a6147"
}

fun ItemStack?.getSkyBlockID(): String {
    this ?: return ""
    val tagCompound = getSubCompound("ExtraAttributes", false) ?: return ""

    return tagCompound.let { if (it.hasKey("id")) it.getString("id") else "" }
}

fun NBTTagCompound.getSkyBlockID(): String =
    getCompoundTag("tag").getCompoundTag("ExtraAttributes").let { if (it.hasKey("id")) it.getString("id") else "" }

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

fun Int.insertCommaEvery3Character(): String {
    val builder = StringBuilder()

    for ((index, char) in toString().reversed().withIndex()) {
        builder.append(char)
        if ((index + 1) % 3 == 0 && index + 1 != toString().length) {
            builder.append(",")
        }
    }

    return builder.reversed().toString()
}

fun <T : Comparable<T>> T.compress(min: T = this, max: T = this) =
    if (this < min) min else if (this > max) max else this

fun Int.getHue() = hsbCache[this]?.get(0) ?: run {
    val hsb = Color.RGBtoHSB(getRedInt(), getGreenInt(), getBlueInt(), null)

    return@run hsb.let {

        hsbCache[this] = hsb

        it[0]
    }
}

fun Int.getSaturation() = hsbCache[this]?.get(1) ?: run {
    val hsb = Color.RGBtoHSB(getRedInt(), getGreenInt(), getBlueInt(), null)

    return@run hsb.let {

        hsbCache[this] = hsb

        it[1]
    }
}

fun Int.getBrightness() = hsbCache[this]?.get(2) ?: run {
    val hsb = Color.RGBtoHSB(getRedInt(), getGreenInt(), getBlueInt(), null)

    return@run hsb.let {

        hsbCache[this] = hsb

        it[2]
    }
}

fun Int.toHexString() = String.format("%08x", this)

val mc: Minecraft = Minecraft.getMinecraft()

fun World.getBlockAtPos(pos: BlockPos) = getBlockState(pos).block

inline val LOGGER: Logger
    get() = LogManager.getLogger()

fun mid(value1: Int, value2: Int) = (value1 + value2) / 2

fun mid(value1: Double, value2: Double) = (value1 + value2) / 2.0

fun mid(value1: Float, value2: Float) = (value1 + value2) / 2F

fun Throwable.notifyException() {
    sendClientMessage("§cException Occurred ${javaClass.name} $message")
}


fun Array<out String>.toBlockPos(indexes: IntRange) = BlockPos(
    this[indexes.first].toInt(),
    this[indexes.first + indexes.step].toInt(),
    this[indexes.first + indexes.step * 2].toInt()
)

fun ItemStack.getSkullOwner(): String {

    val nbgTagList = tagCompound?.getCompoundTag("SkullOwner")?.getCompoundTag("Properties")
        ?.getTagList("textures", Constants.NBT.TAG_COMPOUND) ?: return ""

    (nbgTagList as AccessorNBTTagList).tagList.forEach {
        if (it is NBTTagCompound) {
            return it.getString("Value")
        }
    }

    return ""
}

fun <T, R> Collection<T>.transformToList(transform: (T) -> R): List<R> {
    val list = arrayListOf<R>()

    for (value in this) {
        list.add(transform(value))
    }

    return list
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

inline fun Pattern.matchesMatcher(s: String, block: (Matcher) -> Unit) = matcher(s).takeIf { it.matches() }?.also(block)

inline fun Pattern.findMatcher(s: String, block: (Matcher) -> Unit) = matcher(s).takeIf { it.find() }?.also(block)

fun Entity.toVec3() = Vec3(posX, posY, posZ)

fun Double.transformToPrecision(precision: Int): Double {
    if (precision == 0) return roundToInt().toDouble()

    val pow = 10.pow(precision)

    return round(this * pow) / pow
}

val fontRendererNotNull: Boolean
    get() = mc.fontRendererObj != null

fun <T> Collection<T>.convertToStringList(transform: (T) -> String): List<String> {
    val list = arrayListOf<String>()

    for (element in this) {
        list.add(transform(element))
    }

    return list
}

fun URL.downloadToFile(target: File) {
    val inputStream = openStream().buffered()

    target.outputStream().buffered().use {
        it.write(inputStream.readBytes())
        it.flush()
    }
}