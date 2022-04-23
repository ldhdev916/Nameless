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

import com.happyandjust.nameless.core.input.UserInputItem
import com.happyandjust.nameless.core.property.Identifiers
import com.happyandjust.nameless.core.property.PropertyData
import com.happyandjust.nameless.core.value.ChromaColor
import com.happyandjust.nameless.gui.feature.components.*
import gg.essential.vigilance.gui.settings.*

enum class ComponentType {

    SWITCH {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            return SwitchComponent(propertyData.cast()).apply {
                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is Boolean
        }
    },
    SLIDER_DECIMAL {
        override fun getComponent(propertyData: PropertyData): SettingComponent {

            val minValue = propertyData.propertySetting.minValue.toFloat()
            val maxValue = propertyData.propertySetting.maxValue.toFloat()

            return DecimalSliderComponent(propertyData.cast<Number>().toFloat(), minValue, maxValue).apply {
                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is Double || value is Float
        }
    },
    SLIDER {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            val minValue = propertyData.propertySetting.minValueInt
            val maxValue = propertyData.propertySetting.maxValueInt

            return SliderComponent(propertyData.cast(), minValue, maxValue).apply {
                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is Int
        }
    },
    TEXT {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            val placeHolder = propertyData.propertySetting.placeHolder.orEmpty()
            val textComponent = TextComponent(propertyData.cast(), placeHolder, wrap = false, protected = false)

            return textComponent.toFilterTextComponent().apply {
                validator = propertyData.propertySetting.validator

                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is String
        }
    },
    PASSWORD {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            val placeHolder = propertyData.propertySetting.placeHolder.orEmpty()

            val component = TextComponent(propertyData.cast(), placeHolder, wrap = false, protected = true)

            return component.toFilterTextComponent().apply {
                validator = propertyData.propertySetting.validator

                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return false // Only when preferred
        }
    },
    COLOR {
        override fun getComponent(propertyData: PropertyData): SettingComponent {

            val value = propertyData.cast<ChromaColor>()

            val component = ColorComponent(value, true)
            val chromaEnabled = value.chromaEnabled

            return component.toChromaColorComponent(chromaEnabled).apply {
                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is ChromaColor
        }
    },
    BUTTON {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            val placeHolder = propertyData.propertySetting.placeHolder
            return ButtonComponent(placeHolder, propertyData.cast<() -> Unit>())
        }

        override fun isProperData(value: Any): Boolean {
            return isInstance<() -> Unit>(value)
        }

        private inline fun <reified T> isInstance(value: Any) = value is T
    },
    SELECTOR {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            with(propertyData.propertySetting) {
                val allValue = allValueList()
                val selectedIndex = allValue.indexOf(propertyData.propertyValue.getValue())
                val options = allValue.map(stringSerializer)

                return SelectorComponent(selectedIndex, options).apply {
                    onValueChange {
                        propertyData.set(allValue[it as Int])
                    }
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is Enum<*>
        }
    },
    VERTICAL_MOVE {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            val allIdentifiers = propertyData.propertySetting.allIdentifiers

            return VerticalPositionEditableComponent(propertyData.cast(), allIdentifiers).apply {
                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is Identifiers<*>
        }
    },
    MULTI_SELECTOR {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            with(propertyData.propertySetting) {
                val allValue = allValueList()
                val currentList = propertyData.cast<List<Any>>()
                val selected = currentList.map(stringSerializer)
                val all = allValue.map(stringSerializer)

                return MultiSelectorComponent(selected, all).apply {
                    onValueChange {
                        val indexes = (it as List<*>).filterIsInstance<Int>()

                        propertyData.set(indexes.map(allValue::get))
                    }
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is List<*> && value !is Identifiers<*>
        }
    },
    USER_INPUT {
        override fun getComponent(propertyData: PropertyData): SettingComponent {
            val helpers = mutableListOf<InputHelperComponent>(ColorHelperComponent)

            val placeHolders = propertyData.propertySetting.registeredPlaceHolders
            if (placeHolders.isNotEmpty()) {
                helpers.add(ValueHelperComponent(placeHolders))
            }

            return UserInputItemComponent(propertyData.cast(), helpers).apply {
                onValueChange {
                    propertyData.set(it)
                }
            }
        }

        override fun isProperData(value: Any): Boolean {
            return value is UserInputItem
        }
    };

    abstract fun getComponent(propertyData: PropertyData): SettingComponent

    abstract fun isProperData(value: Any): Boolean

    protected inline fun <reified T : Any> PropertyData.cast() = propertyValue.getValue() as T

    protected fun PropertyData.set(value: Any?) {
        propertyValue.setValue(value!!)
    }
}