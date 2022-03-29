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

package com.happyandjust.nameless.features.impl.qol

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.on
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.features.base.SimpleFeature
import com.happyandjust.nameless.features.base.hierarchy
import com.happyandjust.nameless.features.base.parameter
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.games.MurderMystery
import com.happyandjust.nameless.hypixel.murderer.Assassins
import com.happyandjust.nameless.hypixel.murderer.Classic
import com.happyandjust.nameless.hypixel.murderer.Infection
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import java.awt.Color

object MurdererFinder : SimpleFeature(
    "murdererFinder",
    "Murderer Finder",
    "Supports All types of murder mystery in hypixel"
) {

    init {
        hierarchy {

            ::classic {
                with(Classic) {
                    setupHierarchy()
                }
            }

            ::infection {
                with(Infection) {
                    setupHierarchy()
                }
            }

            ::assassins {
                with(Assassins) {
                    setupHierarchy()
                }
            }

            ::glowGold {
                +::goldColor
            }
        }
    }


    val sword_list = setOf(
        Items.iron_sword,
        Items.stone_sword,
        Items.iron_shovel,
        Items.stick,
        Items.wooden_axe,
        Items.wooden_sword,
        Items.stone_shovel,
        Items.blaze_rod,
        Items.diamond_shovel,
        Items.feather,
        Items.pumpkin_pie,
        Items.golden_pickaxe,
        Items.apple,
        Items.name_tag,
        Items.carrot_on_a_stick,
        Items.bone,
        Items.carrot,
        Items.golden_carrot,
        Items.cookie,
        Items.diamond_axe,
        Items.prismarine_shard,
        Items.golden_sword,
        Items.diamond_sword,
        Items.diamond_hoe,
        Items.shears,
        Items.fish,
        Items.boat,
        Items.cookie,
        Items.cooked_beef,
        Items.speckled_melon,
        Item.getItemFromBlock(Blocks.redstone_torch),
        Item.getItemFromBlock(Blocks.sponge),
        Item.getItemFromBlock(Blocks.double_plant),
        Item.getItemFromBlock(Blocks.deadbush),
        Items.quartz,
        Items.dye,
        Items.netherbrick,
        Items.book
    )

    var classic by parameter(true) {
        key = "classic"
        title = "Classic Mode Helper"
    }

    var infection by parameter(true) {
        key = "infection"
        title = "Infection Mode Helper"
    }

    var assassins by parameter(true) {
        key = "assassins"
        title = "Assassins Mode Helper"
    }

    private var glowGold by parameter(true) {
        key = "glowGold"
        title = "Glow Gold Ingot"
        desc = "Glow gold ingot when you are in murder mystery"
    }

    private var goldColor by parameter(Color(255, 128, 0).toChromaColor()) {
        key = "color"
        title = "Gold Ingot Color"
    }

    init {
        on<OutlineRenderEvent>().filter {
            enabled && Hypixel.currentGame is MurderMystery && glowGold && entity is EntityItem && entity.entityItem.item == Items.gold_ingot
        }.subscribe {
            colorInfo = ColorInfo(goldColor.rgb, ColorInfo.ColorPriority.HIGH)
        }
    }
}