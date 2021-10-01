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

import com.happyandjust.nameless.core.Direction
import com.happyandjust.nameless.gui.Rectangle
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import com.happyandjust.nameless.utils.GLScissorStack
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.inventory.Slot
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.nio.FloatBuffer

fun disableAlpha() = GlStateManager.disableAlpha()
fun enableAlpha() = GlStateManager.enableAlpha()
fun alphaFunc(func: Int, ref: Float) = GlStateManager.alphaFunc(func, ref)
fun enableLighting() = GlStateManager.enableLighting()
fun disableLighting() = GlStateManager.disableLighting()
fun enableLight(light: Int) = GlStateManager.enableLight(light)
fun disableLight(light: Int) = GlStateManager.disableLight(light)
fun enableColorMaterial() = GlStateManager.enableColorMaterial()
fun disableColorMaterial() = GlStateManager.disableColorMaterial()
fun colorMaterial(face: Int, mode: Int) = GlStateManager.colorMaterial(face, mode)
fun disableDepth() = GlStateManager.disableDepth()
fun enableDepth() = GlStateManager.enableDepth()
fun depthFunc(depthFunc: Int) = GlStateManager.depthFunc(depthFunc)
fun depthMask(flagIn: Boolean) = GlStateManager.depthMask(flagIn)
fun disableBlend() = GlStateManager.disableBlend()
fun enableBlend() = GlStateManager.enableBlend()
fun blendFunc(srcFactor: Int, dstFactor: Int) = GlStateManager.blendFunc(srcFactor, dstFactor)
fun tryBlendFuncSeparate(srcFactor: Int, dstFactor: Int, srcFactorAlpha: Int, dstFactorAlpha: Int) =
    GlStateManager.tryBlendFuncSeparate(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha)

fun enableFog() = GlStateManager.enableFog()
fun disableFog() = GlStateManager.disableFog()
fun setFog(param: Int) = GlStateManager.setFog(param)
fun setFogDensity(param: Float) = GlStateManager.setFogDensity(param)
fun setFogStart(param: Float) = GlStateManager.setFogStart(param)
fun setFogEnd(param: Float) = GlStateManager.setFogEnd(param)
fun enableCull() = GlStateManager.enableCull()
fun disableCull() = GlStateManager.disableCull()
fun cullFace(mode: Int) = GlStateManager.cullFace(mode)
fun enablePolygonOffset() = GlStateManager.enablePolygonOffset()
fun disablePolygonOffset() = GlStateManager.disablePolygonOffset()
fun doPolygonOffset(factor: Float, units: Float) = GlStateManager.doPolygonOffset(factor, units)
fun enableColorLogic() = GlStateManager.enableColorLogic()
fun disableColorLogic() = GlStateManager.disableColorLogic()
fun colorLogicOp(opcode: Int) = GlStateManager.colorLogicOp(opcode)
fun enableTexGenCoord(p_179087_0_: GlStateManager.TexGen) = GlStateManager.enableTexGenCoord(p_179087_0_)
fun disableTexGenCoord(p_179100_0_: GlStateManager.TexGen) = GlStateManager.disableTexGenCoord(p_179100_0_)
fun texGen(texGen: GlStateManager.TexGen, param: Int) = GlStateManager.texGen(texGen, param)
fun texGen(p_179105_0_: GlStateManager.TexGen, pname: Int, params: FloatBuffer) =
    GlStateManager.texGen(p_179105_0_, pname, params)

fun setActiveTexture(texture: Int) = GlStateManager.setActiveTexture(texture)
fun enableTexture2D() = GlStateManager.enableTexture2D()
fun disableTexture2D() = GlStateManager.disableTexture2D()
fun deleteTexture(texture: Int) = GlStateManager.deleteTexture(texture)
fun bindTexture(texture: Int) = GlStateManager.bindTexture(texture)
fun enableNormalize() = GlStateManager.enableNormalize()
fun disableNormalize() = GlStateManager.disableNormalize()
fun shadeModel(mode: Int) = GlStateManager.shadeModel(mode)
fun enableRescaleNormal() = GlStateManager.enableRescaleNormal()
fun disableRescaleNormal() = GlStateManager.disableRescaleNormal()
fun viewport(x: Int, y: Int, width: Int, height: Int) = GlStateManager.viewport(x, y, width, height)
fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean) =
    GlStateManager.colorMask(red, green, blue, alpha)

fun clearDepth(depth: Double) = GlStateManager.clearDepth(depth)
fun clearColor(red: Float, green: Float, blue: Float, alpha: Float) =
    GlStateManager.clearColor(red, green, blue, alpha)

fun clear(mask: Int) = GlStateManager.clear(mask)
fun matrixMode(mode: Int) = GlStateManager.matrixMode(mode)
fun loadIdentity() = GlStateManager.loadIdentity()
fun pushMatrix() = GlStateManager.pushMatrix()
fun popMatrix() = GlStateManager.popMatrix()
fun getFloat(pname: Int, params: FloatBuffer) = GlStateManager.getFloat(pname, params)
fun ortho(left: Double, right: Double, bottom: Double, top: Double, zNear: Double, zFar: Double) =
    GlStateManager.ortho(left, right, bottom, top, zNear, zFar)

fun rotate(angle: Float, x: Float, y: Float, z: Float) = GlStateManager.rotate(angle, x, y, z)
fun scale(x: Float, y: Float, z: Float) = GlStateManager.scale(x, y, z)
fun scale(x: Double, y: Double, z: Double) = GlStateManager.scale(x, y, z)
fun scale(x: Int, y: Int, z: Int) = GlStateManager.scale(x.toDouble(), y.toDouble(), z.toDouble())
fun translate(x: Float, y: Float, z: Float) = GlStateManager.translate(x, y, z)
fun translate(x: Double, y: Double, z: Double) = GlStateManager.translate(x, y, z)
fun translate(x: Int, y: Int, z: Int) = GlStateManager.translate(x.toDouble(), y.toDouble(), z.toDouble())
fun multMatrix(matrix: FloatBuffer) = GlStateManager.multMatrix(matrix)
fun color(colorRed: Float, colorGreen: Float, colorBlue: Float, colorAlpha: Float) =
    GlStateManager.color(colorRed, colorGreen, colorBlue, colorAlpha)

fun color(colorRed: Float, colorGreen: Float, colorBlue: Float) =
    GlStateManager.color(colorRed, colorGreen, colorBlue)

fun color(rgb: Int) =
    GlStateManager.color(rgb.getRedFloat(), rgb.getGreenFloat(), rgb.getBlueFloat(), rgb.getAlphaFloat())

fun resetColor() = GlStateManager.resetColor()
fun callList(list: Int) = GlStateManager.callList(list)

val tessellator: Tessellator
    get() = Tessellator.getInstance()

fun FontRenderer.drawString(text: String, x: Int, y: Int, color: Int, dropShadow: Boolean) =
    drawString(text, x.toFloat(), y.toFloat(), color, dropShadow)

fun FontRenderer.drawCenteredString(text: String, color: Int, dropShadow: Boolean = false) =
    drawString(text, -(getStringWidth(text) / 2F), -(FONT_HEIGHT / 2F), color, dropShadow)

fun Entity.getRenderPosX(partialTicks: Float) = lastTickPosX + (posX - lastTickPosX) * partialTicks

fun Entity.getRenderPosY(partialTicks: Float) = lastTickPosY + (posY - lastTickPosY) * partialTicks

fun Entity.getRenderPosZ(partialTicks: Float) = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks

fun Entity.getRenderYaw(partialTicks: Float) = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks

fun Entity.getRenderPitch(partialTicks: Float) = prevRotationPitch + (rotationPitch - prevRotationPitch) * partialTicks

fun WorldRenderer.color(rgb: Int): WorldRenderer =
    color(rgb.getRedInt(), rgb.getGreenInt(), rgb.getBlueInt(), rgb.getAlphaInt())

inline fun scissor(
    rectangle: Rectangle,
    scaleFactor: Int = ScaledResolution(mc).scaleFactor,
    block: () -> Unit
) {
    GLScissorStack.push(rectangle, scaleFactor)

    block()

    GLScissorStack.pop(scaleFactor)
}

inline fun glEnable(vararg caps: Int, block: () -> Unit) {
    for (cap in caps) {
        GL11.glEnable(cap)
    }

    block()

    for (cap in caps) {
        GL11.glDisable(cap)
    }
}

inline fun translate(x: Int, y: Int, z: Int, block: () -> Unit) =
    translate(x.toDouble(), y.toDouble(), z.toDouble(), block)

inline fun translate(x: Double, y: Double, z: Double, block: () -> Unit) {
    translate(x, y, z)
    block()
    translate(-x, -y, -z)
}

inline fun matrix(block: () -> Unit) {
    pushMatrix()

    block()

    popMatrix()
}

fun Rectangle.drawRect(color: Int) {
    Gui.drawRect(left, top, right, bottom, color)
}

fun Rectangle.drawGradientRect(direction: Direction, startColor: Int, endColor: Int) {
    disableTexture2D()
    enableBlend()
    disableCull()
    tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
    shadeModel(GL11.GL_SMOOTH)

    val wr = tessellator.worldRenderer
    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)

    when (direction) {
        Direction.LEFT -> {
            wr.pos(right, top, 0).color(startColor).endVertex()
            wr.pos(right, bottom, 0).color(startColor).endVertex()

            wr.pos(left, bottom, 0).color(endColor).endVertex()
            wr.pos(left, top, 0).color(endColor).endVertex()
        }
        Direction.RIGHT -> {
            wr.pos(left, top, 0).color(startColor).endVertex()
            wr.pos(left, bottom, 0).color(startColor).endVertex()

            wr.pos(right, bottom, 0).color(endColor).endVertex()
            wr.pos(right, top, 0).color(endColor).endVertex()
        }
        Direction.UP -> {
            wr.pos(left, bottom, 0).color(startColor).endVertex()
            wr.pos(right, bottom, 0).color(startColor).endVertex()

            wr.pos(right, top, 0).color(endColor).endVertex()
            wr.pos(left, top, 0).color(endColor).endVertex()
        }
        Direction.DOWN -> {
            wr.pos(left, top, 0).color(startColor).endVertex()
            wr.pos(right, top, 0).color(startColor).endVertex()

            wr.pos(right, bottom, 0).color(endColor).endVertex()
            wr.pos(right, top, 0).color(endColor).endVertex()
        }
    }

    tessellator.draw()

    shadeModel(GL11.GL_FLAT)
    enableCull()
    disableBlend()
    enableTexture2D()
}

/**
 * Draw inward outline box (actually 4 lines)
 */
fun Rectangle.drawOutlineBox(color: Int, outlineWidth: Int = 1) {

    disableTexture2D()
    enableBlend()
    tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0)
    disableCull()
    color(color)

    val wr = tessellator.worldRenderer

    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)

    // left
    wr.pos(left, top, 0).endVertex()
    wr.pos(left + outlineWidth, top, 0).endVertex()
    wr.pos(left + outlineWidth, bottom, 0).endVertex()
    wr.pos(left, bottom, 0).endVertex()

    // top

    wr.pos(left, top, 0).endVertex()
    wr.pos(right, top, 0).endVertex()
    wr.pos(right, top + outlineWidth, 0).endVertex()
    wr.pos(left, top + outlineWidth, 0).endVertex()

    // right

    wr.pos(right - outlineWidth, top, 0).endVertex()
    wr.pos(right, top, 0).endVertex()
    wr.pos(right, bottom, 0).endVertex()
    wr.pos(right - outlineWidth, bottom, 0).endVertex()

    // bottom

    wr.pos(left, bottom - outlineWidth, 0).endVertex()
    wr.pos(right, bottom - outlineWidth, 0).endVertex()
    wr.pos(right, bottom, 0).endVertex()
    wr.pos(left, bottom, 0).endVertex()

    tessellator.draw()

    disableBlend()
    enableCull()
    enableTexture2D()
}

inline fun disableEntityShadow(block: () -> Unit) {
    val entityShadow = mc.gameSettings.entityShadows

    mc.gameSettings.entityShadows = false

    block()

    mc.gameSettings.entityShadows = entityShadow
}

fun GuiContainer.drawOnSlot(slot: Slot, color: Int) {
    val left = (this as AccessorGuiContainer).guiLeft + slot.xDisplayPosition
    val top = (this as AccessorGuiContainer).guiTop + slot.yDisplayPosition

    Gui.drawRect(left, top, left + 16, top + 16, color)
}

fun Rectangle.drawChromaRect(direction: Direction, startHue: Float = 0F) {
    disableTexture2D()
    enableBlend()
    disableCull()
    shadeModel(GL11.GL_SMOOTH)
    tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

    val addEach = when (direction) {
        Direction.UP, Direction.DOWN -> height / 360.0
        Direction.RIGHT, Direction.LEFT -> width / 360.0
    }
    val starting = when (direction) {
        Direction.LEFT -> right
        Direction.UP -> bottom
        Direction.RIGHT -> left
        Direction.DOWN -> top
    }

    val wr = tessellator.worldRenderer

    wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR)

    repeat(361) {
        val currentPosition = starting + (addEach * it)
        val color = Color.HSBtoRGB(it / 360F + startHue, 1f, 1f)

        when (direction) {
            Direction.LEFT, Direction.RIGHT -> {
                wr.pos(currentPosition, top.toDouble(), 0.0).color(color).endVertex()
                wr.pos(currentPosition, bottom.toDouble(), 0.0).color(color).endVertex()
            }
            Direction.UP, Direction.DOWN -> {
                wr.pos(left.toDouble(), currentPosition, 0.0).color(color).endVertex()
                wr.pos(right.toDouble(), currentPosition, 0.0).color(color).endVertex()
            }
        }
    }

    tessellator.draw()

    shadeModel(GL11.GL_FLAT)
    enableCull()
    disableBlend()
    enableTexture2D()
}