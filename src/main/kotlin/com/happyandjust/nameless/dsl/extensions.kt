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

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.happyandjust.nameless.config.ConfigMap
import com.happyandjust.nameless.core.FAIRY_SOUL
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.skyblock.ItemRarity
import com.mojang.authlib.GameProfile
import gg.essential.api.EssentialAPI
import gg.essential.api.utils.WebUtil
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTUtil
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.eventhandler.Event
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.Reader
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

private val RARITY_PATTERN = Pattern.compile("(§[0-9a-f]§l§ka§r )?([§0-9a-fk-or]+)(?<rarity>[A-Z]+)")
private val md = MessageDigest.getInstance("MD5")
private val md5Cache = ConfigMap.StringConfigMap("md5")
private val decimalFormat =
    DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).apply { maximumFractionDigits = 640 }

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
    val values = ItemRarity.values()

    return List(lore.tagCount()) {
        RARITY_PATTERN.findMatcher(lore.getStringTagAt(it)) {
            values.find { rarity -> group("rarity").startsWith(rarity.loreName) }
        }
    }.firstOrNull()
}

fun String.getMD5() =
    md5Cache.getOrPut(this) { md.digest(toByteArray()).joinToString("") { "%02x".format(it) } }

fun String.copyToClipboard() =
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(this), null)

fun Double.formatDouble(): String = decimalFormat.format(this)

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

val Int.red
    get() = this shr 16 and 255

val Int.green
    get() = this shr 8 and 255

val Int.blue
    get() = this and 255

val Int.alpha
    get() = this shr 24 and 255

fun Int.withAlpha(alpha: Int) = this and ((alpha and 255 shl 24) or 0xFFFFFF)

fun Int.withAlpha(alpha: Float) = withAlpha((alpha * 255).toInt())

fun Int.insertCommaEvery3Character() = toString().reversed().chunked(3).joinToString(",").reversed()

inline val mc: Minecraft
    get() = Minecraft.getMinecraft()

fun World.getBlockAtPos(pos: BlockPos): Block = getBlockState(pos).block

fun BlockPos.toVec3() = Vec3(x.toDouble(), y.toDouble(), z.toDouble())

inline val LOGGER: Logger
    get() = LogManager.getLogger()

fun Throwable.notifyException() {
    sendClientMessage("§cException Occurred ${javaClass.name} $message")
    printStackTrace()
}

fun ItemStack.getSkullOwner(): String {

    return if (hasTagCompound() && tagCompound.hasKey("SkullOwner")) {
        NBTUtil.readGameProfileFromNBT(tagCompound.getCompoundTag("SkullOwner")).getSkullOwner()
    } else ""
}

fun GameProfile.getSkullOwner() = properties["textures"]?.find { it.name == "textures" }?.value ?: ""

inline fun <T> Pattern.matchesMatcher(s: String, block: Matcher.() -> T) =
    matcher(s).takeIf { it.matches() }?.run(block)

inline fun <T> Pattern.findMatcher(s: String, block: Matcher.() -> T) = matcher(s).takeIf { it.find() }?.run(block)

fun Entity.toVec3() = Vec3(posX, posY, posZ)

fun Double.transformToPrecision(precision: Int): Double {
    if (precision == 0) return roundToInt().toDouble()

    val pow = 10.0.pow(precision)

    return round(this * pow) / pow
}

fun Double.transformToPrecisionString(precision: Int) = transformToPrecision(precision).formatDouble()

operator fun Pair<*, *>.contains(other: Any?) = first == other || second == other

inline fun <reified E> Any?.withInstance(action: E.() -> Unit) {
    if (this is E) {
        action()
    }
}

fun Event.cancel() = apply {
    isCanceled = true
}

fun String.getUUID() = runCatching { EssentialAPI.getMojangAPI().getUUID(this)?.get() }.getOrNull()

fun UUID.getNameHistory() =
    runCatching { EssentialAPI.getMojangAPI().getNameHistory(this) }.getOrNull()?.filterNotNull()

fun String.fetch() = WebUtil.fetchString(this)

fun String.handler() = JsonHandler(fetch())

val Block.displayName: String
    get() = runCatching { ItemStack(this).displayName }.getOrDefault(registryName.split(":")[1])

inline fun <reified T> Gson.fromJson(json: JsonElement): T = fromJson(json, T::class.java)

inline fun <reified T> Gson.fromJson(reader: Reader): T = fromJson(reader, T::class.java)

inline fun <reified T> Gson.fromJson(s: String): T = fromJson(s, T::class.java)