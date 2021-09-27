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
import com.happyandjust.nameless.mixins.accessors.AccessorNBTTagList
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.ItemStack
import net.minecraft.launchwrapper.Launch
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
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow

private val hsbCache = hashMapOf<Int, FloatArray>()
val md = MessageDigest.getInstance("MD5")
private val md5Cache = ConfigMap.StringConfigMap("md5")

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
    if (Hypixel.currentGame != GameType.SKYBLOCK) return ""
    val tagCompound = getSubCompound("ExtraAttributes", false) ?: return ""

    return tagCompound.let { if (it.hasKey("id")) it.getString("id") else "" }
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

val mc: Minecraft
    get() = Minecraft.getMinecraft()

fun World.getBlockAtPos(pos: BlockPos) = getBlockState(pos).block

inline val LOGGER: Logger
    get() = LogManager.getLogger()

fun mid(value1: Int, value2: Int) = (value1 + value2) / 2

fun mid(value1: Double, value2: Double) = (value1 + value2) / 2.0

fun mid(value1: Float, value2: Float) = (value1 + value2) / 2F

val deobfuscated: Boolean
    get() = Launch.blackboard["fml.deobfuscatedEnvironment"] as Boolean


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

fun Entity.rayTraceEntity(
    reachDistance: Double,
    partialTicks: Float,
    calcEntities: List<Entity> = mc.theWorld.loadedEntityList.also { it.removeIf { e -> e == this } }
): List<Entity> {
    val list = arrayListOf<Entity>()
    val eyes = getPositionEyes(partialTicks) // current vector
    val lookVec = getLook(partialTicks) * reachDistance

    val rayTraceDist = rayTrace(reachDistance, partialTicks)?.hitVec?.distanceTo(eyes) ?: reachDistance

    for (entity in calcEntities) {
        val size = entity.collisionBorderSize.toDouble()
        val collisionBox = entity.entityBoundingBox.expand(size, size, size)

        val movingObjectPosition = collisionBox.calculateIntercept(eyes, eyes.add(lookVec))
        if (collisionBox.isVecInside(eyes)) {
            if (rayTraceDist >= 0.0) {
                list.add(entity)
                continue
            }
        }

        movingObjectPosition?.let {
            val d3 = eyes.distanceTo(it.hitVec)
            if (d3 < rayTraceDist || rayTraceDist == 0.0) {
                if (entity == ridingEntity && !canRiderInteract()) {
                    if (rayTraceDist == 0.0) {
                        list.add(entity)
                    }
                } else {
                    list.add(entity)
                }
            }
        }
    }

    return list
}

fun EntityPlayerSP?.inHypixel() = this?.clientBrand?.startsWith("Hypixel BungeeCord") == true

fun Pattern.matchesMatcher(s: String, block: (Matcher) -> Unit) = matcher(s).takeIf { it.matches() }?.also(block)

fun Pattern.findMatcher(s: String, block: (Matcher) -> Unit) = matcher(s).takeIf { it.find() }?.also(block)

fun Entity.toVec3() = Vec3(posX, posY, posZ)

