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

package com.happyandjust.nameless.features.base

import com.happyandjust.nameless.features.settings
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

internal class FeatureParameterTest {

    private fun setupFeature() = mockk<BaseFeature<Boolean>> {
        every { key } returns "category"
        val param = hashMapOf<String, FeatureParameter<*>>()
        every { parameters } returns param
        val preHierarchy = ParameterHierarchy(this)
        every { hierarchy } returns preHierarchy
    }

    @Test
    fun `Test Correct Ordinal`() {
        val feature = setupFeature()

        with(feature) {
            val param1 = parameter(1) {
                key = "param1"
            }
            val param2 = parameter(false) {
                key = "param2"
            }
            val param3 = parameter(0.0) {
                key = "param3"
            }

            executeHierarchy {
                +param1

                +param2

                nonOrdinal {
                    +param3
                }
            }

            param1.settings {
                assertEquals(0, ordinal)
            }
            param2.settings {
                assertEquals(1, ordinal)
            }
            param3.settings {
                assertEquals(0, ordinal)
            }
        }
    }

    @Test
    fun `Test Correct Category`() {
        val feature = setupFeature()

        with(feature) {
            val param1 = parameter(false) {
                key = "param1"
            }

            executeHierarchy {
                +param1
            }

            assertEquals("category", param1.category)
        }
    }

    @Test
    fun `Test Correct Json Key`() {
        val feature = setupFeature()

        with(feature) {
            val param1 = parameter(false) {
                key = "param1"
            }

            val param2 = parameter(1) {
                key = "param2"
            }

            val param3 = parameter(0.0) {
                key = "param3"
            }

            executeHierarchy {
                param1 {
                    param2 {
                        +param3
                    }
                }
            }

            assertEquals("param1", param1.getSaveKey())
            assertEquals("param1_param2", param2.getSaveKey())
            assertEquals("param1_param2_param3", param3.getSaveKey())
        }
    }

    private fun FeatureParameter<*>.getSaveKey(): String {
        val property = FeatureParameter::class.declaredMemberProperties.single { it.name == "jsonSaveKey" }
            .apply { isAccessible = true }

        return property.get(this) as String
    }
}