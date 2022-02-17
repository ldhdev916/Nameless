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

package com.happyandjust.nameless.gui.feature

import com.happyandjust.nameless.core.value.ChromaColor
import com.happyandjust.nameless.gui.feature.components.*
import gg.essential.vigilance.gui.settings.*
import java.awt.Color

enum class ComponentType {

    SWITCH {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) =
            SwitchComponent(propertyData.property() as Boolean).apply {
                onValueChange {
                    propertyData.property.set(it as T)
                }
            }
    },
    SLIDER_DECIMAL {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) = DecimalSliderComponent(
            propertyData.property().let { (it as Number).toFloat() },
            propertyData.propertySetting.minValue.toFloat(),
            propertyData.propertySetting.maxValue.toFloat()
        ).apply {
            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    SLIDER {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) = SliderComponent(
            propertyData.property() as Int,
            propertyData.propertySetting.minValueInt,
            propertyData.propertySetting.maxValueInt
        ).apply {
            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    TEXT {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) = TextComponent(
            propertyData.property() as String,
            propertyData.propertySetting.placeHolder ?: "",
            false,
            false
        ).toFilterTextComponent().apply {

            validator = propertyData.propertySetting.validator

            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    PASSWORD {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) = TextComponent(
            propertyData.property() as String,
            propertyData.propertySetting.placeHolder ?: "",
            false,
            true
        ).toFilterTextComponent().apply {
            validator = propertyData.propertySetting.validator

            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    COLOR {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) = ColorComponent(
            propertyData.property() as Color,
            true
        ).toChromaColorComponent((propertyData.property() as ChromaColor).chromaEnabled).apply {
            onValueChange {
                propertyData.property.set(it as T)
            }
        }
    },
    BUTTON {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) =
            ButtonComponent(propertyData.propertySetting.placeHolder, propertyData.property() as () -> Unit)
    },
    SELECTOR {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) =
            with(propertyData.propertySetting) {
                val allValue = allValueList()
                SelectorComponent(
                    allValue.indexOf(propertyData.property()),
                    allValue.map(stringSerializer)
                ).apply {
                    onValueChange {
                        propertyData.property.set(allValue[it as Int])
                    }
                }
            }
    },
    VERTIAL_MOVE {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) =
            VerticalPositionEditableComponent(
                propertyData.propertySetting.allIdentifiers,
                propertyData.property() as List<Identifier>
            ).apply {
                onValueChange {
                    propertyData.property.set(it as T)
                }
            }
    },
    MULTI_SELECTOR {
        override fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>) =
            with(propertyData.propertySetting) {
                val list = propertyData.property() as List<E>
                val allValue = listAllValueList()
                MultiSelectorComponent(
                    list.map(listStringSerializer),
                    allValue.map(listStringSerializer)
                ).apply {
                    onValueChange {
                        val indexes = it as List<Int>
                        propertyData.property.set(indexes.map(allValue::get) as T)
                    }
                }
            }
    };

    abstract fun <T : Any, E : Any> getComponent(propertyData: PropertyData<T, E>): SettingComponent
}