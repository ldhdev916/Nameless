package com.happyandjust.nameless.features.base

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.test.assertEquals

internal class FeatureParameterTest {

    @Test
    fun `Test Correct Category`() {
        val feature = mockk<BaseFeature<Boolean>> {
            every { key } returns "category"
            every { parameters } returns hashMapOf()
        }

        with(feature) {
            val param1 = parameter(false) {
                key = "param1"
            }

            hierarchy {
                +param1
            }

            ParameterHierarchy.executeAll()

            assertEquals(param1.category, "category")
        }
    }

    @Test
    fun `Test Correct Json Key`() {
        val feature = mockk<BaseFeature<Boolean>> {
            every { key } returns "feature"
            every { parameters } returns hashMapOf()
        }

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

            hierarchy {
                param1 {
                    param2 {
                        +param3
                    }
                }
            }

            ParameterHierarchy.executeAll()

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