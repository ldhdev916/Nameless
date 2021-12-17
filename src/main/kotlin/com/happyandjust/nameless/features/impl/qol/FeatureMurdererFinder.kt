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

package com.happyandjust.nameless.features.impl.qol

import com.google.gson.JsonObject
import com.happyandjust.nameless.core.JsonHandler
import com.happyandjust.nameless.core.TickTimer
import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.*
import com.happyandjust.nameless.features.*
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.MurdererMode
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.serialization.converters.CBoolean
import com.happyandjust.nameless.serialization.converters.CChromaColor
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemMap
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S09PacketHeldItemChange
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color
import java.awt.Point

object FeatureMurdererFinder : SimpleFeature(
    Category.QOL,
    "murdererfinder",
    "Murderer Finder",
    "Supports All types of murder mystery in hypixel"
) {
    private val sword_list = hashSetOf(
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
    private var pathsToTarget = emptyList<BlockPos>()
    private val pathTimer = TickTimer.withSecond(1.5)

    private val murderers = MurdererSet()
    private val survivors = hashSetOf<String>()

    private var alpha: String? = null
        set(value) {
            if (field != value && value != null && value != mc.thePlayer.name) {
                sendPrefixMessage("§aAlpha: $value")
            }
            field = value
        }
    private val ALPHA_FOUND = "The alpha, (?<alpha>\\w+), has been revealed by \\w+!".toPattern()
    private val INFECTED = "\\w+(\\s\\w+)? infected (?<infected>\\w+)".toPattern()
    private val ENVIRONMENT_INFECTED = "(?<infected>\\w+) was infected by the environment!".toPattern()
    private val ALPHA_LEFT =
        "The alpha left the game\\. (?<name>\\w+) was chosen to be the new alpha infected!".toPattern()

    private var targetName: String? = null
    private var prevTargetName: String? = null

    private val assassinMapHash = hashMapOf<String, String>()

    fun fetchAssassinData() {
        val json = JsonHandler(ResourceLocation("nameless", "assassins.json")).read(JsonObject())
        assassinMapHash.putAll(json.entrySet().map { it.key to it.value.asString })
    }

    private var glowGold by FeatureParameter(
        0,
        "murderer",
        "glowgold",
        "Glow Gold Ingot",
        "Glow gold ingot when you are in murder mystery",
        false,
        CBoolean
    )

    @SubParameterOf("glowGold")
    private var goldColor by FeatureParameter(
        0,
        "murderer",
        "goldcolor",
        "Gold Ingot Color",
        "",
        Color(255, 128, 0).toChromaColor(),
        CChromaColor
    )

    private var murdererColor by FeatureParameter(
        1,
        "murderer",
        "murderercolor",
        "Murderer Color",
        "",
        Color.blue.toChromaColor(),
        CChromaColor
    )

    @InCategory("Infection")
    private var glowSurvivor by FeatureParameter(
        0,
        "murderer",
        "glowsurvivor",
        "Glow Survivor",
        "Glow survivor in hypixel murderer INFECTION",
        true,
        CBoolean
    )

    @SubParameterOf("glowSurvivor")
    private var survivorColor by FeatureParameter(
        0,
        "murderer",
        "survivorcolor",
        "Survivor Color",
        "",
        Color.green.toChromaColor(),
        CChromaColor
    )

    @InCategory("Infection")
    private var alphaColor by FeatureParameter(
        1,
        "murderer",
        "alphacolor",
        "Alpha Color",
        "",
        Color(128, 0, 128).toChromaColor(),
        CChromaColor
    )

    @InCategory("Assassin")
    private var targetColor by FeatureParameter(
        0,
        "murderer",
        "targetcolor",
        "Target Color",
        "Target glow color in hypixel murderer ASSASSIN",
        Color.red.toChromaColor(),
        CChromaColor
    )

    @InCategory("Assassin")
    private var targetArrow by FeatureParameter(
        1,
        "murderer",
        "targetarrow",
        "Render Direction Arrow to Target",
        "Render arrow shape on your screen which is pointing the target",
        true,
        CBoolean
    )

    @InCategory("Assassin")
    private var targetPath by FeatureParameter(
        2,
        "murderer",
        "targetpath",
        "Show Paths to Target",
        "",
        false,
        CBoolean
    )

    init {
        on<SpecialTickEvent>().filter {
            checkForEnabledAndMurderMystery() && Hypixel.getProperty<MurdererMode>(
                PropertyKey.MURDERER_TYPE
            ) == MurdererMode.ASSASSIN
        }.subscribe {
            if (targetName == null) {
                targetName = getTargetName()?.takeIf { it != prevTargetName }?.also {
                    sendClientMessage("§eYour new target is §c$it")
                }
            } else {
                if (targetPath && pathTimer.update().check()) {
                    getPlayerByName(targetName)?.let {
                        pathsToTarget = ModPathFinding(BlockPos(it), false).findPath()
                    }
                }
            }
        }

        on<HypixelServerChangeEvent>().subscribe {
            murderers.clear()
            survivors.clear()
            alpha = null
            targetName = null
            pathsToTarget = emptyList()
        }

        on<ClientChatReceivedEvent>().filter { checkForEnabledAndMurderMystery() }.subscribe {
            when (Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)) {
                MurdererMode.INFECTION -> {
                    ALPHA_FOUND.matchesMatcher(pureText) {
                        alpha = it.group("alpha")
                        // this is why I set alpha to String, player may not be loaded when alpha is found
                    }
                    INFECTED.matchesMatcher(pureText) {
                        val infectedName = it.group("infected")
                        murderers.add(infectedName)
                        survivors.remove(infectedName) // infected cannot be survivor smh

                        // same as reason why I set to String
                    }
                    ENVIRONMENT_INFECTED.matchesMatcher(pureText) {
                        val infectedName = it.group("infected")
                        murderers.add(infectedName)
                        survivors.remove(infectedName)
                    }
                    ALPHA_LEFT.matchesMatcher(pureText) {
                        alpha = it.group("name")
                    }
                }
                MurdererMode.ASSASSIN -> {
                    if (pureText == "Your kill contract has been updated!") {
                        cancel()
                        prevTargetName = targetName
                        targetName = null // reset
                    }
                }
                else -> {}
            }
        }

        on<RenderWorldLastEvent>().filter {
            checkForEnabledAndMurderMystery() && Hypixel.getProperty<MurdererMode>(
                PropertyKey.MURDERER_TYPE
            ) == MurdererMode.ASSASSIN && targetPath && pathsToTarget.isNotEmpty()
        }.subscribe {
            RenderUtils.drawPath(pathsToTarget, Color.red.rgb, partialTicks)
        }

        on<PacketEvent.Received>().filter { checkForEnabledAndMurderMystery() }.subscribe {
            val entityPlayer: EntityPlayer
            val heldItem: ItemStack?
            when (val msg = packet) {
                is S04PacketEntityEquipment -> {
                    entityPlayer = mc.theWorld.getEntityByID(msg.entityID) as? EntityPlayer ?: return@subscribe
                    heldItem = msg.itemStack
                }
                is S09PacketHeldItemChange -> {
                    entityPlayer = mc.thePlayer
                    heldItem = mc.thePlayer.inventory.getStackInSlot(msg.heldItemHotbarIndex)
                }
                else -> return@subscribe
            }
            heldItem ?: return@subscribe

            val mode = Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)
            val playerName = entityPlayer.name

            if (mode == MurdererMode.ASSASSIN) return@subscribe

            val isInfection = mode == MurdererMode.INFECTION

            if (heldItem.item in sword_list) { // found
                if (isInfection && entityPlayer.getEquipmentInSlot(3)?.item == Items.iron_chestplate) {
                    alpha = playerName
                } else {
                    murderers.add(playerName)

                    if (isInfection) {
                        survivors.remove(playerName)
                    }
                }
            } else if (isInfection) {
                when (heldItem.item) {
                    Items.bow -> {
                        if ("§c" in heldItem.displayName) {
                            alpha = playerName
                        } else if ("§a" in heldItem.displayName) {
                            survivors.add(playerName)
                        }
                    }
                    Items.arrow -> { // there's no FAKE ARROW
                        survivors.add(playerName)
                    }
                }

            }
        }
    }

    private fun checkForEnabledAndMurderMystery() = enabled && Hypixel.currentGame == GameType.MURDER_MYSTERY

    init {
        on<OutlineRenderEvent>().filter { checkForEnabledAndMurderMystery() }.subscribe {
            val mode = Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)
            entity.withInstance<EntityPlayer> {
                val color = when {
                    mode != MurdererMode.ASSASSIN && name in murderers && (mode != MurdererMode.INFECTION || alpha != name) -> murdererColor
                    mode == MurdererMode.INFECTION -> if (name in survivors) {
                        if (glowSurvivor) survivorColor else return@subscribe
                    } else if (name == alpha) alphaColor else return@subscribe
                    mode == MurdererMode.ASSASSIN && targetName == name.trim() -> targetColor
                    else -> return@subscribe
                }.rgb

                colorInfo = ColorInfo(color, ColorInfo.ColorPriority.HIGH)
                return@subscribe
            }
            entity.withInstance<EntityItem> {
                if (entityItem.item == Items.gold_ingot && glowGold) {
                    colorInfo = ColorInfo(goldColor.rgb, ColorInfo.ColorPriority.HIGH)
                }
            }
        }

        on<SpecialOverlayEvent>().filter { checkForEnabledAndMurderMystery() && targetArrow }.subscribe {
            getPlayerByName(targetName)?.let {
                RenderUtils.drawDirectionArrow(it.toVec3(), Color.red.rgb)
            }
        }
    }

    //from here, assassins
    private fun getTargetName(): String? {
        val itemStack = mc.thePlayer.inventory.getStackInSlot(3) ?: return null // map which contains your target data
        val map = itemStack.item
        if (map !is ItemMap || !itemStack.displayName.contains("§c")) return null // is there a KILL CONTRACT?

        return assassinMapHash[getNamePoints(
            map.getMapData(
                itemStack,
                mc.theWorld
            ).colors
        ).joinToString("") { it.toMapString() }.getMD5()]
    }

    private fun getPlayerByName(name: String?): EntityPlayer? {
        name ?: return null // we don't need to scan if name is null
        return mc.theWorld.playerEntities.singleOrNull { it.name.trim() == name }
    }

    /**
     * @return point data which contains NAME in map
     */
    private fun getNamePoints(colors: ByteArray): List<Point> {
        val points = arrayListOf<Point>()
        for (y in 105..111) {
            for (x in 0 until 63) {
                val index = y * 128 + x
                val color = colors[index]

                when (color.toInt()) {
                    53, 54 -> continue // this is background color
                    else -> points.add(Point(x, y))
                }
            }
        }

        return points
    }

    class MurdererSet : HashSet<String>() {

        override fun add(element: String): Boolean {
            return super.add(element).also {
                if (it && Hypixel.currentGame == GameType.MURDER_MYSTERY && Hypixel.getProperty<MurdererMode>(
                        PropertyKey.MURDERER_TYPE
                    ) == MurdererMode.CLASSIC && element != mc.thePlayer.name
                ) {
                    sendPrefixMessage("§cMurderer: $element")
                }
            }
        }
    }

    private fun Point.toMapString() = "($x,$y)"
}