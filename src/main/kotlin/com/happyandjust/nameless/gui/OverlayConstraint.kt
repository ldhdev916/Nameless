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

import com.happyandjust.nameless.core.value.Overlay
import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.HeightConstraint
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.SuperConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.State

class OverlayConstraint(overlay: Overlay) : PositionConstraint, HeightConstraint {

    private var overlayState: State<Overlay> = BasicState(overlay)

    var overlay
        get() = overlayState.get()
        set(value) {
            overlayState.set(value)
        }

    fun bindOverlay(newState: State<Overlay>) = apply {
        overlayState = newState
    }

    override var cachedValue = 0f
    override var constrainTo: UIComponent? = null
    override var recalculate = true

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {

    }

    override fun getXPositionImpl(component: UIComponent): Float {
        return overlay.x.toFloat()
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        return overlay.y.toFloat()
    }

    /**
     * Only use for Text Scale
     */
    override fun getHeightImpl(component: UIComponent): Float {
        return overlay.scale.toFloat()
    }

    override fun to(component: UIComponent): SuperConstraint<Float> {
        throw UnsupportedOperationException("Constraint.to(UIComponent) is not available in this context!")
    }

    companion object {
        fun Overlay.constraint() = OverlayConstraint(this)
    }
}