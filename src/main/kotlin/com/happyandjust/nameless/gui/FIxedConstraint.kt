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

package com.happyandjust.nameless.gui

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

class FixedConstraint(private val parentConstraint: SuperConstraint<Float>) : MasterConstraint {
    override var cachedValue = 0f
    override var constrainTo: UIComponent? = null
    override var recalculate = true

    override fun getHeightImpl(component: UIComponent) = (parentConstraint as HeightConstraint).getHeightImpl(component)

    override fun getHeight(component: UIComponent) = getHeightImpl(component)

    override fun getRadiusImpl(component: UIComponent) = (parentConstraint as RadiusConstraint).getRadiusImpl(component)

    override fun getRadius(component: UIComponent) = getRadiusImpl(component)

    override fun getWidthImpl(component: UIComponent) = (parentConstraint as WidthConstraint).getWidthImpl(component)

    override fun getWidth(component: UIComponent) = getWidthImpl(component)

    override fun getXPositionImpl(component: UIComponent) =
        (parentConstraint as XConstraint).getXPositionImpl(component)

    override fun getXPosition(component: UIComponent) = getXPositionImpl(component)

    override fun getYPositionImpl(component: UIComponent) =
        (parentConstraint as YConstraint).getYPositionImpl(component)

    override fun getYPosition(component: UIComponent) = getYPositionImpl(component)

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {

    }

    override fun to(component: UIComponent): SuperConstraint<Float> {
        error("")
    }


}

fun SuperConstraint<Float>.fixed() = FixedConstraint(this)