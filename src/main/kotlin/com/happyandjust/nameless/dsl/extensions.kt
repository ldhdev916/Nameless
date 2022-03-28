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

package com.happyandjust.nameless.dsl

import com.happyandjust.nameless.config.ConfigMap.Companion.configMap
import com.mojang.authlib.GameProfile
import gg.essential.api.EssentialAPI
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTUtil
import net.minecraft.scoreboard.ScorePlayerTeam
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3
import net.minecraft.world.World
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

private val md = MessageDigest.getInstance("MD5")
private val md5Cache = configMap<String>("md5")
private val decimalFormat =
    DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH)).apply { maximumFractionDigits = 640 }
inline val mc: Minecraft
    get() = Minecraft.getMinecraft()

inline val LOGGER: Logger
    get() = LogManager.getLogger()


fun String.getMD5() =
    md5Cache.getOrPut(this) { md.digest(toByteArray()).joinToString("", transform = "%02x"::format) }

fun String.copyToClipboard() =
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(this), null)

fun Double.formatDouble(): String = decimalFormat.format(this)

fun BlockPos.getAxisAlignedBB() =
    AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), x + 1.0, y + 1.0, z + 1.0)

fun String.stripControlCodes(): String = StringUtils.stripControlCodes(this)

fun Int.withAlpha(alpha: Int) = (this or 0xFF000000.toInt()) and ((alpha and 0xFF shl 24) or 0xFFFFFF)

fun Int.withAlpha(alpha: Float) = withAlpha((alpha * 255).toInt())

fun Int.insertCommaEvery3Character() = toString().reversed().chunked(3).joinToString(",").reversed()

fun World.getBlockAtPos(pos: BlockPos): Block = getBlockState(pos).block

fun BlockPos.toVec3() = Vec3(x.toDouble(), y.toDouble(), z.toDouble())

fun Entity.toVec3() = Vec3(posX, posY, posZ)

fun ItemStack.getSkullOwner(): String {
    return if (hasTagCompound() && tagCompound.hasKey("SkullOwner")) {
        NBTUtil.readGameProfileFromNBT(tagCompound.getCompoundTag("SkullOwner")).getSkullOwner()
    } else ""
}

fun GameProfile.getSkullOwner() = properties["textures"]?.find { it.name == "textures" }?.value ?: ""

inline fun <T> Pattern.matchesMatcher(s: String, block: Matcher.() -> T) =
    matcher(s).takeIf { it.matches() }?.run(block)

inline fun <T> Pattern.findMatcher(s: String, block: Matcher.() -> T) = matcher(s).takeIf { it.find() }?.run(block)

fun Double.withPrecision(precision: Int): Double {
    if (precision == 0) return roundToInt().toDouble()

    val pow = 10.0.pow(precision)

    return round(this * pow) / pow
}

fun Double.withPrecisionText(precision: Int) = withPrecision(precision).formatDouble()

inline fun <reified T> withInstance(target: Any?, action: T.() -> Unit) {
    if (target is T) target.action()
}

fun getUUID(username: String) = runCatching { EssentialAPI.getMojangAPI().getUUID(username)?.get() }.getOrNull()

fun getNameHistory(uuid: UUID) =
    runCatching { EssentialAPI.getMojangAPI().getNameHistory(uuid) }.getOrNull()?.filterNotNull()

val Block.displayName: String
    get() = runCatching { ItemStack(this).displayName }.getOrDefault(registryName.split(":")[1])

inline fun <reified T : Enum<T>> listEnum() = enumValues<T>().toList()

fun World.getPlayersInTab() = mc.netHandler?.playerInfoMap.orEmpty().mapNotNull { playerInfo ->
    playerInfo.gameProfile.name?.let(this::getPlayerEntityByName)
}

/**
 * Taken from Danker's Skyblock Mod under GPL-3.0 License
 *
 * https://github.com/bowser0000/SkyblockMod/blob/master/LICENSE
 * @author bowser0000
 */
fun World.getSidebarLines(): List<String> {
    val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return emptyList()

    var scores = scoreboard.getSortedScores(objective).filterNotNull()

    val list = scores.filter { it.playerName?.startsWith("#") == false }

    scores = if (list.size > 15) list.drop(list.size - 15) else list

    return scores.mapNotNull {
        val team = scoreboard.getPlayersTeam(it.playerName) ?: return@mapNotNull null

        ScorePlayerTeam.formatPlayerName(team, it.playerName).stripControlCodes()
            .filter { char -> char.code in 21..126 }
    }
}