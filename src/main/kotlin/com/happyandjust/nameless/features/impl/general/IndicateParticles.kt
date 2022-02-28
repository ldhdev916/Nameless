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

package com.happyandjust.nameless.features.impl.general

import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.dsl.tessellator
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.mixins.accessors.AccessorEntityFX
import gg.essential.elementa.utils.invisible
import net.minecraft.client.particle.EntityFX
import net.minecraft.client.renderer.GlStateManager.disableDepth
import net.minecraft.client.renderer.GlStateManager.enableDepth
import net.minecraft.client.renderer.WorldRenderer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.Entity
import net.minecraft.util.EnumParticleTypes
import net.minecraftforge.event.world.WorldEvent
import org.lwjgl.opengl.GL11
import java.awt.Color

object IndicateParticles : SimpleFeature(
    "indicateParticles",
    "Indicate Particles",
    "Indicate certain particle types you selected, set color alpha to 0 if you don't want custom color"
) {

    private val entities = hashMapOf<EntityFX, Color>()

    init {
        for (particle in EnumParticleTypes.values()) {

            val key = particle.particleID.toString()

            val name = particle.name.split("_").joinToString(" ") { "${it[0]}${it.drop(1).lowercase()}" }

            parameter(false) {
                matchKeyCategory()
                this.key = key
                title = name

                parameter(Color.red.invisible().toChromaColor()) {
                    matchKeyCategory()
                    this.key = "color"
                    title = "Color of Particle"
                }
            }
        }
    }

    @JvmStatic
    fun checkAndAdd(particleId: Int, entityFX: EntityFX) {
        if (!enabled) return
        val parameter = getParameter<Boolean>(particleId.toString())

        if (!parameter.value) return
        entities[entityFX] = parameter.getParameterValue("color")
    }

    init {
        on<WorldEvent.Load>().subscribe { entities.clear() }
    }

    @JvmStatic
    fun renderParticle(
        instance: EntityFX,
        worldRendererIn: WorldRenderer,
        entityIn: Entity,
        partialTicks: Float,
        rotationX: Float,
        rotationZ: Float,
        rotationYZ: Float,
        rotationXY: Float,
        rotationXZ: Float,
        shouldBeginAgain: Boolean
    ) {
        val render = {
            instance.renderParticle(
                worldRendererIn,
                entityIn,
                partialTicks,
                rotationX,
                rotationZ,
                rotationYZ,
                rotationXY,
                rotationXZ
            )
        }
        val color = entities[instance] ?: run {
            render()
            return
        }
        val begin =
            { tessellator.worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP) }
        disableDepth()

        if (color.alpha != 0) {
            with(instance as AccessorEntityFX) {
                setParticleRed(color.red / 255f)
                setParticleGreen(color.green / 255f)
                setParticleBlue(color.blue / 255f)
                setParticleAlpha(color.alpha / 255f)
            }
        }

        render()

        if (shouldBeginAgain) {
            tessellator.draw()
            begin()
        }
        enableDepth()
        return

    }
}