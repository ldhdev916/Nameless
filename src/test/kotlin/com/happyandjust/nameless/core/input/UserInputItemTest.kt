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

package com.happyandjust.nameless.core.input

import net.minecraft.util.EnumChatFormatting
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UserInputItemTest {

    private fun doAssert(item: UserInputItem) {
        assertEquals(item, UserInputItem.parseFromPreviewString(item.asPreviewString()))
    }

    @Test
    fun textOnly_parseFromPreviewString_TextInputItem() {
        doAssert(TextInputItem("Hello!"))
    }

    @Test
    fun colorOnly_parseFromPreviewString_ColorInputItem() {
        doAssert(ColorInputItem(EnumChatFormatting.YELLOW))
    }

    @Test
    fun valueOnly_parseFromPreviewString_ValueInputItem() {
        doAssert(ValueInputItem("nameless"))
    }

    @Test
    fun allTogether_parseFromPreviewString_CompositeInputItem() {
        doAssert(buildComposite {
            color { EnumChatFormatting.RED }
            text { "Hello, " }
            value { "world" }
            text { " No you!" }
        })
    }
}