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

import com.happyandjust.nameless.core.enums.Direction
import com.happyandjust.nameless.core.value.Overlay
import com.happyandjust.nameless.mixins.accessors.AccessorGuiContainer
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.inventory.Slot
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Rectangle
import kotlin.math.max

val tessellator: Tessellator
    get() = Tessellator.getInstance()

fun color(rgb: Int) {
    val red = (rgb shr 16 and 255) / 255f
    val green = (rgb shr 8 and 255) / 255f
    val blue = (rgb and 255) / 255f
    val alpha = (rgb shr 24 and 255) / 255f
    color(red, green, blue, alpha)
}

fun FontRenderer.drawCenteredString(text: String, color: Int, dropShadow: Boolean = false) =
    drawString(text, -(getStringWidth(text) / 2F), -(FONT_HEIGHT / 2F), color, dropShadow)

fun Entity.getRenderPosX(partialTicks: Float) = lastTickPosX + (posX - lastTickPosX) * partialTicks

fun Entity.getRenderPosY(partialTicks: Float) = lastTickPosY + (posY - lastTickPosY) * partialTicks

fun Entity.getRenderPosZ(partialTicks: Float) = lastTickPosZ + (posZ - lastTickPosZ) * partialTicks

fun Entity.getRenderYaw(partialTicks: Float) = prevRotationYaw + (rotationYaw - prevRotationYaw) * partialTicks

fun WorldRenderer.color(rgb: Int): WorldRenderer = apply {
    val red = rgb shr 16 and 255
    val green = rgb shr 8 and 255
    val blue = rgb and 255
    val alpha = rgb shr 24 and 255
    color(red, green, blue, alpha)
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

fun Rectangle.drawChromaRect(direction: Direction, startHue: Float = 0F, alpha: Int = 255) {

    val total = 60f

    disableTexture2D()
    enableBlend()
    enableDepth()
    disableCull()
    shadeModel(GL11.GL_SMOOTH)
    tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

    val addEach = when (direction) {
        Direction.UP, Direction.DOWN -> height / total
        Direction.RIGHT, Direction.LEFT -> width / total
    }

    val starting = when (direction) {
        Direction.LEFT -> right
        Direction.UP -> bottom
        Direction.RIGHT -> left
        Direction.DOWN -> top
    }

    val wr = tessellator.worldRenderer

    wr.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR)

    repeat(total.toInt()) {
        val currentPosition = starting + (addEach * it).toDouble()
        val color = Color.HSBtoRGB(it / total + startHue, 1f, 1f) and ((alpha shl 24) or 0xFFFFFF)

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

val Rectangle.left
    get() = x

val Rectangle.top
    get() = y

val Rectangle.right
    get() = x + width

val Rectangle.bottom
    get() = y + height

fun setup(overlay: Overlay) {
    translate(overlay.x.toDouble(), overlay.y.toDouble(), 0.0)
    scale(overlay.scale, overlay.scale, 1.0)
}

fun wrapScaleTo1Block(text: String) = max(mc.fontRendererObj.getStringWidth(text), mc.fontRendererObj.FONT_HEIGHT)