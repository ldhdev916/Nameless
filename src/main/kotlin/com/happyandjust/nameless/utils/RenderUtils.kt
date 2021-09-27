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
import com.happyandjust.nameless.devqol.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11

object RenderUtils {

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
            glEnable(GL11.GL_STENCIL_TEST) {

                GL11.glClearStencil(0)
                clear(GL11.GL_STENCIL_BUFFER_BIT)

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
            }
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
            glEnable(GL11.GL_POINT_SMOOTH) {
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
            }
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

    fun draw3DString(text: String, vec3: Vec3, scale: Double, color: Int, partialTicks: Float) {
        val render = mc.renderViewEntity ?: return

        val x = vec3.xCoord - render.getRenderPosX(partialTicks)
        val y = vec3.yCoord - render.getRenderPosY(partialTicks)
        val z = vec3.zCoord - render.getRenderPosZ(partialTicks)

        val scale = scale / (mc.fontRendererObj.FONT_HEIGHT)

        matrix {
            translate(x, y, z)
            disableDepth()
            enableBlend()
            tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)

            rotate(-mc.renderManager.playerViewY, 0f, 1f, 0f)
            rotate(mc.renderManager.playerViewX, 1f, 0f, 0f)

            scale(-scale, -scale, -scale)
            // text are first inverted

            mc.fontRendererObj.drawString(
                text,
                -(mc.fontRendererObj.getStringWidth(text) / 2),
                -(mc.fontRendererObj.FONT_HEIGHT / 2),
                color
            )

            scale(1, 1, 1)
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
            MathHelper.wrapAngleTo180_float(Location(mc.thePlayer).also { it.lookAt(Location(target)) }.yaw) - MathHelper.wrapAngleTo180_float(
                mc.thePlayer.rotationYaw
            )

        matrix {
            translate(sr.scaledWidth / 2, sr.scaledHeight / 2, 0)

            val dist = mc.thePlayer.getDistance(target.xCoord, target.yCoord, target.zCoord)

            val minScale = 1.5
            val maxScale = 4.0

            val percent = dist / 40

            val scale = (maxScale - (maxScale - minScale) * percent).compress(minScale, maxScale)

            rotate(diffYaw, 0f, 0f, 1f)

            scale(scale, scale, 1.0)

            mc.fontRendererObj.drawCenteredString(arrow, color)

            scale(1, 1, 1)
            color(1f, 1f, 1f, 1f)

        }


    }
}
