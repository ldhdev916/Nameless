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

package com.happyandjust.nameless.gui.feature

import com.happyandjust.nameless.core.ChromaColor
import com.happyandjust.nameless.gui.feature.components.Identifier
import com.happyandjust.nameless.gui.feature.components.VerticalPositionEditableComponent
import com.happyandjust.nameless.gui.feature.components.toChromaColorComponent
import com.happyandjust.nameless.gui.feature.components.toFilterTextComponent
import gg.essential.vigilance.gui.settings.*
import java.awt.Color

enum class ComponentType {

    SWITCH {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent =
            SwitchComponent(propertyData.property() as Boolean).apply {
                onValueChange {
                    propertyData.property.set(it as T)
                }
            }
    },
    SLIDER_DECIMAL {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent = DecimalSliderComponent(
            propertyData.property().let { (it as Number).toFloat() },
            propertyData.minValue.toFloat(),
            propertyData.maxValue.toFloat()
        ).apply {
            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    SLIDER {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent = SliderComponent(
            propertyData.property() as Int,
            propertyData.minValue.toInt(),
            propertyData.maxValue.toInt()
        ).apply {
            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    TEXT {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent = TextComponent(
            propertyData.property() as String,
            propertyData.placeHolder ?: "",
            false,
            false
        ).toFilterTextComponent().apply {

            validator = propertyData.validator

            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    PASSWORD {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent = TextComponent(
            propertyData.property() as String,
            propertyData.placeHolder ?: "",
            false,
            true
        ).toFilterTextComponent().apply {
            validator = propertyData.validator

            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    COLOR {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent = ColorComponent(
            propertyData.property() as Color,
            false
        ).toChromaColorComponent((propertyData.property() as ChromaColor).chromaEnabled).apply {
            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    BUTTON {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent =
            ButtonComponent(propertyData.placeHolder, propertyData.property() as () -> Unit)

    },
    SELECTOR {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent =
            SelectorComponent(
                propertyData.allEnumList.indexOf(propertyData.property() as Enum<*>),
                propertyData.allEnumList.map { it.name }).apply {
                onValueChange {
                    propertyData.property.set(propertyData.allEnumList[it as Int] as T)
                }
            }
    },
    VERTIAL_MOVE {
        override fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent =
            VerticalPositionEditableComponent(
                propertyData.allIdentifiers,
                propertyData.property() as List<Identifier>
            ).apply {
                onValueChange {
                    propertyData.property.set(it as T)
                }
            }
    };

    abstract fun <T> getComponent(propertyData: PropertyData<T>): SettingComponent
}