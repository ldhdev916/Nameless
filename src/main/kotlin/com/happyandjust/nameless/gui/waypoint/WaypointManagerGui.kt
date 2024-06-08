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

package com.happyandjust.nameless.gui.waypoint

import com.happyandjust.nameless.gui.feature.ColorCache
import com.happyandjust.nameless.listener.WaypointListener
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.CopyConstraintFloat
import gg.essential.elementa.constraints.FillConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.utils.withAlpha
import gg.essential.universal.GuiScale
import gg.essential.vigilance.gui.settings.ButtonComponent
import net.minecraft.util.BlockPos

class WaypointManagerGui : WindowScreen(
    ElementaVersion.V5,
    newGuiScale = GuiScale.scaleForScreenSize().ordinal,
    restoreCurrentGuiOnClose = true,
    drawDefaultBackground = false
) {

    init {

        UIBlock(ColorCache.background.withAlpha(0.3f)).constrain {
            width = 100.percent()
            height = 100.percent()
        } childOf window

        UIText("Waypoints").constrain {
            x = CenterConstraint()
            y = 20.pixels()

            textScale = 2.5.pixels()
        } childOf window

        UIBlock(ColorCache.divider).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(5f)

            width = 60.percent()
        } childOf window
    }


    private val waypointContainer = UIContainer().constrain {
        x = CenterConstraint()
        y = SiblingConstraint(5f)

        width = 100.percent()
        height = 80.percent()
    } childOf window

    private val dummyWaypointComponent = DummyWaypointComponent() childOf waypointContainer

    init {
        ButtonComponent("Add New") {
            with(WaypointListener.waypointInfos) {
                val info = WaypointListener.WaypointInfo("Waypoint #${size + 1}", BlockPos.ORIGIN, true)
                add(info)

                SetWaypointComponent(this@WaypointManagerGui, info) childOf scroller
            }

        }.constrain {
            x = SiblingConstraint(10f) boundTo dummyWaypointComponent
            y = CopyConstraintFloat() boundTo dummyWaypointComponent
        } childOf window
    }

    val scroller = ScrollComponent("No Waypoints.", innerPadding = 2f).constrain {

        y = SiblingConstraint(3f)

        width = 100.percent()
        height = FillConstraint(false)
    } childOf waypointContainer


    init {

        WaypointListener.waypointInfos.map { SetWaypointComponent(this, it) }.forEach {
            it.constrain {
                width = max(dummyWaypointComponent.constraints.width, width)
            } childOf scroller
        }
    }
}