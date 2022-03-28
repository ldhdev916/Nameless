package com.happyandjust.nameless.dsl

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ExtensionsKtTest {

    private val color1 = 0x00FF0000
    private val color2 = 0xFFFF0000.toInt()

    @Test
    fun withAlpha() {
        assertEquals(color1.withAlpha(255), color2)
        assertEquals(color2.withAlpha(0), color1)
    }

    @Test
    fun testWithAlpha() {
        assertEquals(color1.withAlpha(1f), color2)
        assertEquals(color2.withAlpha(0f), color1)
    }
}