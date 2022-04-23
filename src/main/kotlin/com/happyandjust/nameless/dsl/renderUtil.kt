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

import com.happyandjust.nameless.Location
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.*
import org.lwjgl.opengl.GL11.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun AxisAlignedBB.drawOutlinedBox(color: Int, partialTicks: Float) {
    default3DSetup(partialTicks) {

        disableTexture2D()
        enableBlend()
        glLineWidth(2f)
        disableDepth()
        defaultBlend()

        color(color)

        translate(minX, minY, minZ)

        val dx = maxX - minX
        val dy = maxY - minY
        val dz = maxZ - minZ

        with(tessellator.worldRenderer) {
            //top
            begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION)

            pos(0, dy, 0).endVertex()
            pos(dx, dy, 0).endVertex()
            pos(dx, dy, dz).endVertex()
            pos(0, dy, dz).endVertex()

            tessellator.draw()

            //bottom
            begin(GL_LINE_LOOP, DefaultVertexFormats.POSITION)

            pos(0, 0, 0).endVertex()
            pos(dx, 0, 0).endVertex()
            pos(dx, 0, dz).endVertex()
            pos(0, 0, dz).endVertex()

            tessellator.draw()

            //side
            begin(GL_LINES, DefaultVertexFormats.POSITION)

            pos(0, 0, 0).endVertex()
            pos(0, dy, 0).endVertex()

            pos(dx, 0, 0).endVertex()
            pos(dx, dy, 0).endVertex()

            pos(dx, 0, dz).endVertex()
            pos(dx, dy, dz).endVertex()

            pos(0, 0, dz).endVertex()
            pos(0, dy, dz).endVertex()

            tessellator.draw()
        }

        enableDepth()
        disableBlend()
        enableTexture2D()
    }
}

fun Vec3.drawPoint(color: Int, size: Number, partialTicks: Float) {
    default3DSetup(partialTicks) {

        glEnable(GL_POINT_SMOOTH)
        disableTexture2D()
        enableBlend()
        disableDepth()
        defaultBlend()
        glPointSize(size.toFloat())
        color(color)

        with(tessellator.worldRenderer) {
            begin(GL_POINTS, DefaultVertexFormats.POSITION)

            pos(xCoord, yCoord, zCoord).endVertex()

            tessellator.draw()
        }

        color(1f, 1f, 1f, 1f)
        disableBlend()
        enableDepth()
        enableTexture2D()
        glDisable(GL_POINT_SMOOTH)
    }
}

fun AxisAlignedBB.drawFilledBox(color: Int, partialTicks: Float) {
    default3DSetup(partialTicks) {
        enableBlend()
        disableDepth()
        disableCull()
        defaultBlend()

        disableLighting()
        setActiveTexture(OpenGlHelper.defaultTexUnit)
        color(color)
        disableTexture2D()
        setActiveTexture(OpenGlHelper.lightmapTexUnit)
        setActiveTexture(OpenGlHelper.defaultTexUnit)

        translate(minX, minY, minZ)

        val dx = maxX - minX
        val dy = maxY - minY
        val dz = maxZ - minZ

        with(tessellator.worldRenderer) {
            begin(GL_QUADS, DefaultVertexFormats.POSITION)

            //top
            pos(0, dy, 0).endVertex()
            pos(dx, dy, 0).endVertex()
            pos(dx, dy, dz).endVertex()
            pos(0, dy, dz).endVertex()

            //bottom
            pos(0, 0, 0).endVertex()
            pos(dx, 0, 0).endVertex()
            pos(dx, 0, dz).endVertex()
            pos(0, 0, dz).endVertex()

            //side
            pos(0, 0, 0).endVertex()
            pos(0, dy, 0).endVertex()
            pos(0, dy, dz).endVertex()
            pos(0, 0, dz).endVertex()

            pos(0, 0, dz).endVertex()
            pos(0, dy, dz).endVertex()
            pos(dx, dy, dz).endVertex()
            pos(dx, 0, dz).endVertex()

            pos(dx, 0, dz).endVertex()
            pos(dx, dy, dz).endVertex()
            pos(dx, dy, 0).endVertex()
            pos(dx, 0, 0).endVertex()

            pos(dx, 0, 0).endVertex()
            pos(dx, dy, 0).endVertex()
            pos(0, dy, 0).endVertex()
            pos(0, 0, 0).endVertex()

            tessellator.draw()
        }

        color(1f, 1f, 1f, 1f)
        disableBlend()
        enableTexture2D()
        enableCull()
        enableDepth()
    }
}

fun Vec3.drawString(text: String, numberOfBlocksInText: Number, color: Int, partialTicks: Float) {
    default3DSetup(partialTicks) {
        translate(xCoord, yCoord, zCoord)

        val scale = numberOfBlocksInText.toDouble() / wrapScaleTo1Block(text)

        disableDepth()
        enableBlend()
        defaultBlend()

        with(mc.renderManager) {
            rotate(-playerViewY, 0f, 1f, 0f)
            rotate(playerViewX, 1f, 0f, 0f)
        }

        scale(-scale, -scale, -scale)
        mc.fontRendererObj.drawCenteredString(text, color)

        disableBlend()
        enableDepth()
    }
}

fun BlockPos.drawStringAtCenter(text: String, numberOfBlocksInText: Number, color: Int, partialTicks: Float) {
    Vec3(x + 0.5, y + 0.5, z + 0.5).drawString(text, numberOfBlocksInText, color, partialTicks)
}

fun Iterable<Vec3>.drawCurvedLine(color: Int, lineWidth: Number, partialTicks: Float) {
    default3DSetup(partialTicks) {
        disableTexture2D()
        enableBlend()
        disableDepth()
        defaultBlend()
        glLineWidth(lineWidth.toFloat())
        color(color)

        with(tessellator.worldRenderer) {
            begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

            forEach {
                pos(it.xCoord, it.yCoord, it.zCoord).endVertex()
            }

            tessellator.draw()
        }

        color(1f, 1f, 1f, 1f)
        enableDepth()
        disableBlend()
        enableTexture2D()
    }
}

fun List<BlockPos>.drawPaths(color: Int, partialTicks: Float) {
    map { Vec3(it.x + 0.5, it.y.toDouble(), it.z + 0.5) }.drawCurvedLine(color, 3, partialTicks)
}

private const val ARROW = "⬆"
private const val MIN_SCALE_DIRECTION = 1.5
private const val MAX_SCALE_DIRECTION = 4.0

fun Vec3.drawDirectionArrow(color: Int) {
    val lookAtYaw = Location(mc.thePlayer).lookAt(Location(this)).yaw
    val playerYaw = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw)
    val diffYaw = MathHelper.wrapAngleTo180_float(lookAtYaw) - playerYaw
    val dist = mc.thePlayer.getDistance(xCoord, yCoord, zCoord)

    matrix {
        val sr = ScaledResolution(mc)
        translate(sr.scaledWidth / 2f, sr.scaledHeight / 2f)

        val percent = dist / 40
        val scale = (MAX_SCALE_DIRECTION - (MAX_SCALE_DIRECTION - MIN_SCALE_DIRECTION) * percent)
            .coerceIn(MIN_SCALE_DIRECTION, MAX_SCALE_DIRECTION)

        rotate(diffYaw, 0f, 0f, 1f)
        scale(scale, scale)

        mc.fontRendererObj.drawCenteredString(ARROW, color)

        scale()
        color(1f, 1f, 1f, 1f)
    }
}

private val beaconBeam by lazy { ResourceLocation("textures/entity/beacon_beam.png") }
private const val HEIGHT = 300
private const val BOTTOM_OFFSET = 0
private const val TOP_OFFSET = HEIGHT + BOTTOM_OFFSET

/**
 * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
 *
 * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
 *
 * Modified
 *
 * @author Moulberry
 */
fun Vec3.renderBeaconBeam(rgb: Int, alphaMultiplier: Float, partialTicks: Float) {
    val render = mc.renderViewEntity ?: return
    val (renderX, renderY, renderZ) = render.getRenderPos(partialTicks)

    val x = xCoord - renderX
    val y = yCoord - renderY
    val z = zCoord - renderZ

    mc.textureManager.bindTexture(beaconBeam)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, 10497f)
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, 10497f)

    disableLighting()
    enableCull()
    enableTexture2D()
    tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE, GL_ONE, GL_ZERO)
    enableBlend()
    defaultBlend()

    val time = mc.theWorld.totalWorldTime + partialTicks

    val d1 = MathHelper.func_181162_h(-time * 0.2 - MathHelper.floor_double(-time * 0.1))

    val r = (rgb shr 16 and 255) / 255f
    val g = (rgb shr 8 and 255) / 255f
    val b = (rgb and 255) / 255f

    val d2 = time * 0.025 * -1.5
    val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
    val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
    val d6 = 0.5 + cos(d2 + PI / 4.0) * 0.2
    val d7 = 0.5 + sin(d2 + PI / 4.0) * 0.2
    val d8 = 0.5 + cos(d2 + 3.9269908169872414) * 0.2
    val d9 = 0.5 + sin(d2 + 3.9269908169872414) * 0.2
    val d10 = 0.5 + cos(d2 + 5.497787143782138) * 0.2
    val d11 = 0.5 + sin(d2 + 5.497787143782138) * 0.2
    val d14 = -1.0 + d1
    val d15 = HEIGHT * 2.5 + d14

    with(tessellator.worldRenderer) {
        begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

        pos(x + d4, y + TOP_OFFSET, z + d5).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d4, y + BOTTOM_OFFSET, z + d5).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d6, y + BOTTOM_OFFSET, z + d7).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d6, y + TOP_OFFSET, z + d7).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d10, y + TOP_OFFSET, z + d11).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d10, y + BOTTOM_OFFSET, z + d11).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d8, y + BOTTOM_OFFSET, z + d9).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d8, y + TOP_OFFSET, z + d9).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d6, y + TOP_OFFSET, z + d7).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d6, y + BOTTOM_OFFSET, z + d7).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d10, y + BOTTOM_OFFSET, z + d11).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d10, y + TOP_OFFSET, z + d11).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d8, y + TOP_OFFSET, z + d9).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        pos(x + d8, y + BOTTOM_OFFSET, z + d9).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d4, y + BOTTOM_OFFSET, z + d5).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        pos(x + d4, y + TOP_OFFSET, z + d5).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()

        tessellator.draw()
    }

    disableCull()

    val d12 = -1 + d1
    val d13 = HEIGHT + d12

    with(tessellator.worldRenderer) {

        begin(GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        pos(x + 0.2, y + TOP_OFFSET, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.2, y + BOTTOM_OFFSET, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.8, y + BOTTOM_OFFSET, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.8, y + TOP_OFFSET, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.8, y + TOP_OFFSET, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.8, y + BOTTOM_OFFSET, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.2, y + BOTTOM_OFFSET, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.2, y + TOP_OFFSET, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.8, y + TOP_OFFSET, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.8, y + BOTTOM_OFFSET, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.8, y + BOTTOM_OFFSET, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.8, y + TOP_OFFSET, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.2, y + TOP_OFFSET, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()
        pos(x + 0.2, y + BOTTOM_OFFSET, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.2, y + BOTTOM_OFFSET, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        pos(x + 0.2, y + TOP_OFFSET, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier).endVertex()

        tessellator.draw()
    }
}

private inline fun default3DSetup(partialTicks: Float, block: () -> Unit) {
    val render = mc.renderViewEntity ?: return

    matrix {
        val (renderX, renderY, renderZ) = render.getRenderPos(partialTicks)

        translate(-renderX, -renderY, -renderZ) {
            block()
        }
    }
}

private fun defaultBlend() {
    tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
}