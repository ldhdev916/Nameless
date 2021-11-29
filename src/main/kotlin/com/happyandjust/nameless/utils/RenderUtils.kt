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

import com.happyandjust.nameless.Location
import com.happyandjust.nameless.dsl.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.*
import org.lwjgl.opengl.GL11
import kotlin.math.cos
import kotlin.math.sin


object RenderUtils {

    private val beaconBeam = ResourceLocation("textures/entity/beacon_beam.png")

    /**
     * Taken from NotEnoughUpdates under Creative Commons Attribution-NonCommercial 3.0
     *
     * https://github.com/Moulberry/NotEnoughUpdates/blob/master/LICENSE
     *
     * Modified
     *
     * @author Moulberry
     */
    fun renderBeaconBeam(pos: Vec3, rgb: Int, alphaMultiplier: Float, partialTicks: Float) {

        val render = mc.renderViewEntity ?: return

        val x = pos.xCoord - render.getRenderPosX(partialTicks)
        val y = pos.yCoord - render.getRenderPosY(partialTicks)
        val z = pos.zCoord - render.getRenderPosZ(partialTicks)

        val height = 300
        val bottomOffset = 0
        val topOffset = bottomOffset + height

        val worldrenderer = tessellator.worldRenderer

        mc.textureManager.bindTexture(beaconBeam)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, 10497.0f)
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, 10497.0f)

        disableLighting()
        enableCull()
        enableTexture2D()
        tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO)
        enableBlend()
        tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

        val time = mc.theWorld.totalWorldTime + partialTicks.toDouble()

        val d1 = MathHelper.func_181162_h(-time * 0.2 - MathHelper.floor_double(-time * 0.1).toDouble())

        val r = rgb.red / 255f
        val g = rgb.green / 255f
        val b = rgb.blue / 255f

        val d2 = time * 0.025 * -1.5
        val d4 = 0.5 + cos(d2 + 2.356194490192345) * 0.2
        val d5 = 0.5 + sin(d2 + 2.356194490192345) * 0.2
        val d6 = 0.5 + cos(d2 + Math.PI / 4.0) * 0.2
        val d7 = 0.5 + sin(d2 + Math.PI / 4.0) * 0.2
        val d8 = 0.5 + cos(d2 + 3.9269908169872414) * 0.2
        val d9 = 0.5 + sin(d2 + 3.9269908169872414) * 0.2
        val d10 = 0.5 + cos(d2 + 5.497787143782138) * 0.2
        val d11 = 0.5 + sin(d2 + 5.497787143782138) * 0.2
        val d14 = -1.0 + d1
        val d15 = height * 2.5 + d14

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)

        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d6, y + topOffset, z + d7).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d6, y + bottomOffset, z + d7).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d10, y + bottomOffset, z + d11).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d10, y + topOffset, z + d11).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d8, y + topOffset, z + d9).tex(1.0, d15).color(r, g, b, alphaMultiplier).endVertex()
        worldrenderer.pos(x + d8, y + bottomOffset, z + d9).tex(1.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d4, y + bottomOffset, z + d5).tex(0.0, d14).color(r, g, b, 1.0f).endVertex()
        worldrenderer.pos(x + d4, y + topOffset, z + d5).tex(0.0, d15).color(r, g, b, alphaMultiplier).endVertex()

        tessellator.draw()

        disableCull()

        val d12 = -1.0 + d1
        val d13 = height + d12

        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR)
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.2).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.2).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + bottomOffset, z + 0.8).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.8, y + topOffset, z + 0.8).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.8).tex(1.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.8).tex(1.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + bottomOffset, z + 0.2).tex(0.0, d12).color(r, g, b, 0.25f).endVertex()
        worldrenderer.pos(x + 0.2, y + topOffset, z + 0.2).tex(0.0, d13).color(r, g, b, 0.25f * alphaMultiplier)
            .endVertex()

        tessellator.draw()
    }

    fun drawOutlinedBox(axisAlignedBB: AxisAlignedBB, color: Int, partialTicks: Float) {
        val render = mc.renderViewEntity ?: return

        matrix {
            translate(
                -render.getRenderPosX(partialTicks),
                -render.getRenderPosY(partialTicks),
                -render.getRenderPosZ(partialTicks)
            )

            disableTexture2D()
            enableBlend()
            GL11.glLineWidth(2F)
            disableDepth()
            tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            color(color)

            val wr = tessellator.worldRenderer

            axisAlignedBB.apply {
                translate(minX, minY, minZ)

                val x = maxX - minX
                val y = maxY - minY
                val z = maxZ - minZ
                wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION)

                //top
                wr.pos(0.0, y, 0.0).endVertex()
                wr.pos(x, y, 0.0).endVertex()
                wr.pos(x, y, z).endVertex()
                wr.pos(0.0, y, z).endVertex()

                tessellator.draw()

                wr.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION)
                //bottom
                wr.pos(0.0, 0.0, 0.0).endVertex()
                wr.pos(x, 0.0, 0.0).endVertex()
                wr.pos(x, 0.0, z).endVertex()
                wr.pos(0.0, 0.0, z).endVertex()

                tessellator.draw()

                //sides
                wr.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)

                wr.pos(0.0, 0.0, 0.0).endVertex()
                wr.pos(0.0, y, 0.0).endVertex()

                wr.pos(x, 0.0, 0.0).endVertex()
                wr.pos(x, y, 0.0).endVertex()

                wr.pos(x, 0.0, z).endVertex()
                wr.pos(x, y, z).endVertex()

                wr.pos(0.0, 0.0, z).endVertex()
                wr.pos(0.0, y, z).endVertex()

                tessellator.draw()
            }
            enableDepth()
            disableBlend()
            enableTexture2D()
        }
    }

    fun changeEntityColor(entity: Entity, color: Int, partialTicks: Float) {
        val render = mc.renderViewEntity?.takeUnless { it == entity } ?: return

        disableEntityShadow {
            GL11.glEnable(GL11.GL_STENCIL_TEST)
            GL11.glClearStencil(0)
            clear(GL11.GL_STENCIL_BUFFER_BIT)

            GL11.glStencilMask(0xFF)
            GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE)
            GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF)

            matrix {
                translate(
                    -render.getRenderPosX(partialTicks),
                    -render.getRenderPosY(partialTicks),
                    -render.getRenderPosZ(partialTicks)
                )

                mc.renderManager.doRenderEntity(
                    entity,
                    entity.getRenderPosX(partialTicks),
                    entity.getRenderPosY(partialTicks),
                    entity.getRenderPosZ(partialTicks),
                    entity.getRenderYaw(partialTicks),
                    partialTicks,
                    true
                )
            }

            GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)
            GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF)

            drawBox(entity.entityBoundingBox.expand(1.0, 1.0, 1.0), color, partialTicks)
            GL11.glDisable(GL11.GL_STENCIL_TEST)
        }

    }

    fun draw3DPoint(point: Vec3, color: Int, size: Double, partialTicks: Float) {
        val render = mc.renderViewEntity ?: return

        matrix {
            translate(
                -render.getRenderPosX(partialTicks),
                -render.getRenderPosY(partialTicks),
                -render.getRenderPosZ(partialTicks)
            )

            GL11.glEnable(GL11.GL_POINT_SMOOTH)
            disableTexture2D()
            enableBlend()
            disableDepth()
            tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
            GL11.glPointSize(size.toFloat())
            color(color)

            val wr = tessellator.worldRenderer

            wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION)

            wr.pos(point.xCoord, point.yCoord, point.zCoord).endVertex()

            tessellator.draw()

            color(1f, 1f, 1f, 1f)
            disableBlend()
            enableDepth()
            enableTexture2D()
            GL11.glDisable(GL11.GL_POINT_SMOOTH)
        }
    }

    fun drawBox(axisAlignedBB: AxisAlignedBB, color: Int, partialTicks: Float) {
        val render = mc.renderViewEntity ?: return

        matrix {
            translate(
                -(render.getRenderPosX(partialTicks)),
                -(render.getRenderPosY(partialTicks)),
                -(render.getRenderPosZ(partialTicks))
            )
            enableBlend()
            disableDepth()
            disableCull()
            tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            disableLighting()
            setActiveTexture(OpenGlHelper.defaultTexUnit)
            color(color)
            disableTexture2D()
            setActiveTexture(OpenGlHelper.lightmapTexUnit)
            disableTexture2D()
            setActiveTexture(OpenGlHelper.defaultTexUnit)

            val wr = tessellator.worldRenderer

            axisAlignedBB.apply {
                val x = maxX - minX
                val y = maxY - minY
                val z = maxZ - minZ
                translate(minX, minY, minZ) {

                    wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION)
                    //top
                    wr.pos(0.0, y, 0.0).endVertex()
                    wr.pos(x, y, 0.0).endVertex()
                    wr.pos(x, y, z).endVertex()
                    wr.pos(0.0, y, z).endVertex()

                    // bottom
                    wr.pos(0.0, 0.0, 0.0).endVertex()
                    wr.pos(x, 0.0, 0.0).endVertex()
                    wr.pos(x, 0.0, z).endVertex()
                    wr.pos(0.0, 0.0, z).endVertex()

                    // sides
                    wr.pos(0.0, 0.0, 0.0).endVertex()
                    wr.pos(0.0, y, 0.0).endVertex()
                    wr.pos(0.0, y, z).endVertex()
                    wr.pos(0.0, 0.0, z).endVertex()

                    wr.pos(0.0, 0.0, z).endVertex()
                    wr.pos(0.0, y, z).endVertex()
                    wr.pos(x, y, z).endVertex()
                    wr.pos(x, 0.0, z).endVertex()

                    wr.pos(x, 0.0, z).endVertex()
                    wr.pos(x, y, z).endVertex()
                    wr.pos(x, y, 0.0).endVertex()
                    wr.pos(x, 0.0, 0.0).endVertex()

                    wr.pos(x, 0.0, 0.0).endVertex()
                    wr.pos(x, y, 0.0).endVertex()
                    wr.pos(0.0, y, 0.0).endVertex()
                    wr.pos(0.0, 0.0, 0.0).endVertex()

                    tessellator.draw()
                }
            }

            color(1f, 1f, 1f, 1f)
            disableBlend()
            enableTexture2D()
            enableCull()
            enableDepth()

        }
    }

    fun draw3DString(text: String, pos: BlockPos, scale: Double, color: Int, partialTicks: Float) {
        draw3DString(text, Vec3(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5), scale, color, partialTicks)
    }

    fun draw3DString(text: String, vec3: Vec3, scale: Double, color: Int, partialTicks: Float) {
        val render = mc.renderViewEntity ?: return

        val x = vec3.xCoord - render.getRenderPosX(partialTicks)
        val y = vec3.yCoord - render.getRenderPosY(partialTicks)
        val z = vec3.zCoord - render.getRenderPosZ(partialTicks)

        val scale = scale / wrapScaleTo1Block(text)

        matrix {
            translate(x, y, z)
            disableDepth()
            enableBlend()
            tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
            rotate(mc.renderManager.playerViewX, 1f, 0f, 0f)

            scale(-scale, -scale, -scale)
            mc.fontRendererObj.drawCenteredString(text, color)

            disableBlend()
            enableDepth()
        }
    }

    fun drawCurveLine(points: List<Vec3>, color: Int, lineWidth: Double, partialTicks: Float) {
        val render = mc.renderViewEntity ?: return

        val x = render.getRenderPosX(partialTicks)
        val y = render.getRenderPosY(partialTicks)
        val z = render.getRenderPosZ(partialTicks)

        matrix {
            translate(-x, -y, -z)
            disableTexture2D()
            enableBlend()
            disableDepth()
            blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glLineWidth(lineWidth.toFloat())
            color(color)

            val wr = tessellator.worldRenderer

            wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

            for (point in points) {
                wr.pos(point.xCoord, point.yCoord, point.zCoord).endVertex()
            }

            tessellator.draw()

            color(1f, 1f, 1f, 1f)
            enableDepth()
            disableBlend()
            enableTexture2D()
        }
    }

    fun drawPath(paths: List<BlockPos>, color: Int, partialTicks: Float) {
        if (paths.isEmpty()) return
        val render = mc.renderViewEntity ?: return

        val x = render.getRenderPosX(partialTicks)
        val y = render.getRenderPosY(partialTicks)
        val z = render.getRenderPosZ(partialTicks)

        matrix {
            translate(-x, -y, -z)
            disableDepth()
            enableBlend()
            disableTexture2D()
            blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            color(color)
            GL11.glLineWidth(3f)

            val wr = tessellator.worldRenderer

            wr.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION)

            for (pos in paths) {

                wr.pos(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5).endVertex()
            }

            tessellator.draw()

            color(1f, 1f, 1f, 1f)
            enableTexture2D()
            disableBlend()
            enableDepth()

        }
    }

    fun drawDirectionArrow(target: Vec3, color: Int) {
        val arrow = "⬆"

        val sr = ScaledResolution(mc)

        val diffYaw =
            MathHelper.wrapAngleTo180_float(Location(mc.thePlayer).lookAt(Location(target)).yaw) - MathHelper.wrapAngleTo180_float(
                mc.thePlayer.rotationYaw
            )

        matrix {
            translate(sr.scaledWidth / 2, sr.scaledHeight / 2, 0)

            val dist = mc.thePlayer.getDistance(target.xCoord, target.yCoord, target.zCoord)

            val minScale = 1.5
            val maxScale = 4.0

            val percent = dist / 40

            val scale = (maxScale - (maxScale - minScale) * percent).coerceIn(minScale, maxScale)

            rotate(diffYaw, 0f, 0f, 1f)

            scale(scale, scale, 1.0)

            mc.fontRendererObj.drawCenteredString(arrow, color)

            scale(1, 1, 1)
            color(1f, 1f, 1f, 1f)

        }


    }
}
