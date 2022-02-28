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

package com.happyandjust.nameless.mixinhooks

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.enums.OutlineMode
import com.happyandjust.nameless.dsl.matrix
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.listener.OutlineHandleListener
import com.happyandjust.nameless.mixins.accessors.AccessorRenderGlobal
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import java.lang.reflect.Method

object RenderGlobalHook {

    private val FLOAT_BUFFER = BufferUtils.createFloatBuffer(4)
    private var isFastRender: Method? = null
    private var isShaders: Method? = null
    private var isAntialiasing: Method? = null
    private var confirmedNoOptifine = false

    fun canDisplayOutline(): Boolean {
        if (hasOptifine()) {
            val isFastRenderValue: Boolean
            val isShadersValue: Boolean
            val isAntialiasingValue: Boolean

            try {
                isFastRenderValue = isFastRender!!(null) as Boolean
                isShadersValue = isShaders!!(null) as Boolean
                isAntialiasingValue = isAntialiasing!!(null) as Boolean
            } catch (ignored: Exception) {
                return false
            }

            return !isFastRenderValue && !isShadersValue && !isAntialiasingValue
        }

        return true
    }

    private fun hasOptifine(): Boolean {
        if (confirmedNoOptifine) return false
        if (isFastRender != null && isShaders != null && isAntialiasing != null) return true


        return try {
            val config = Class.forName("Config")
            isFastRender = config.getMethod("isFastRender")
            isShaders = config.getMethod("isShaders")
            isAntialiasing = config.getMethod("isAntialiasing")

            true
        } catch (ignored: Exception) {
            if (mc.theWorld != null) {
                confirmedNoOptifine = true
            }
            false
        }
    }

    fun renderOutline(entities: List<Entity>, camera: ICamera, x: Double, y: Double, z: Double, partialTicks: Float) {
        if (!canDisplayOutline()) return

        mc.renderGlobal.apply {
            this as AccessorRenderGlobal

            depthFunc(GL11.GL_ALWAYS)
            disableFog()
            entityOutlineFramebuffer.framebufferClear()
            entityOutlineFramebuffer.bindFramebuffer(false)
            mc.theWorld.theProfiler.endStartSection("entityOutlines")
            RenderHelper.disableStandardItemLighting()
            mc.renderManager.setRenderOutlines(true)

            if (Nameless.selectedOutlineMode == OutlineMode.OUTLINE) {
                for (entity in entities) {
                    val flag = with(mc.renderViewEntity) {
                        this is EntityLivingBase && isPlayerSleeping
                    }
                    val flag1 = (entity is EntityPlayer || entity.isInRangeToRender3d(
                        x,
                        y,
                        z
                    )) && (entity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(entity.entityBoundingBox) || entity.riddenByEntity == mc.thePlayer)

                    if ((entity != mc.renderViewEntity || mc.gameSettings.thirdPersonView != 0 || flag) && flag1) {
                        getOutlineColor(entity)?.let {
                            matrix {
                                enableOutlineMode(it)

                                OutlineHandleListener.manualRendering = true
                                mc.renderManager.renderEntitySimple(entity, partialTicks)
                                OutlineHandleListener.manualRendering = false

                                disableOutlineMode()
                            }
                        }
                    }
                }
            }

            mc.renderManager.setRenderOutlines(false)
            RenderHelper.enableStandardItemLighting()
            depthMask(false)
            entityOutlineShader.loadShaderGroup(partialTicks)
            enableLighting()
            depthMask(true)
            mc.framebuffer.bindFramebuffer(false)
            enableFog()
            enableBlend()
            enableColorMaterial()
            depthFunc(GL11.GL_LEQUAL)
            enableDepth()
            enableAlpha()
        }
    }

    private fun enableOutlineMode(color: Int) {

        val red = color shr 16 and 255
        val green = color shr 8 and 255
        val blue = color and 255
        val alpha = color shr 24 and 255

        FLOAT_BUFFER.put(0, red / 255f)
        FLOAT_BUFFER.put(1, green / 255f)
        FLOAT_BUFFER.put(2, blue / 255f)
        FLOAT_BUFFER.put(3, alpha / 255f)

        GL11.glTexEnv(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_COLOR, FLOAT_BUFFER)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL13.GL_COMBINE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL13.GL_CONSTANT)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_REPLACE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }

    private fun disableOutlineMode() {
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_RGB, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_RGB, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_RGB, GL11.GL_SRC_COLOR)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_COMBINE_ALPHA, GL11.GL_MODULATE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_SOURCE0_ALPHA, GL11.GL_TEXTURE)
        GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL13.GL_OPERAND0_ALPHA, GL11.GL_SRC_ALPHA)
    }

    fun getOutlineColor(entity: Entity) =
        OutlineHandleListener.getOutlineColorForEntity(entity)
}