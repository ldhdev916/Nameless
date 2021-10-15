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
import com.happyandjust.nameless.core.*
import com.happyandjust.nameless.devqol.*
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.Category
import com.happyandjust.nameless.features.FeatureParameter
import com.happyandjust.nameless.features.SimpleFeature
import com.happyandjust.nameless.features.listener.*
import com.happyandjust.nameless.hypixel.GameType
import com.happyandjust.nameless.hypixel.Hypixel
import com.happyandjust.nameless.hypixel.MurdererMode
import com.happyandjust.nameless.hypixel.PropertyKey
import com.happyandjust.nameless.pathfinding.ModPathFinding
import com.happyandjust.nameless.serialization.TypeRegistry
import com.happyandjust.nameless.utils.RenderUtils
import net.minecraft.entity.Entity
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
import java.awt.Color
import java.util.regex.Pattern

class FeatureMurdererFinder : SimpleFeature(
    Category.QOL,
    "murdererfinder",
    "Murderer Finder",
    "Supports All types of murder mystery in hypixel"
), ClientTickListener, ServerChangeListener, ChatListener, WorldRenderListener, StencilListener, RenderOverlayListener,
    PacketListener {
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
        Items.dye
    )
    private var pathsToTarget = emptyList<BlockPos>()
    private var pathTick = 0

    private val murderers = hashSetOf<String>()
    private val survivors = hashSetOf<String>()

    private var alpha: String? = null
    private val ALPHA_FOUND = Pattern.compile("The alpha, (?<alpha>\\w+), has been revealed by \\w+!")
    private val INFECTED = Pattern.compile("\\w+(\\s\\w+)? infected (?<infected>\\w+)")
    private val ENVIRONMENT_INFECTED = Pattern.compile("(?<infected>\\w+) was infected by the environment!")
    private val ALPHA_LEFT =
        Pattern.compile("The alpha left the game\\. (?<name>\\w+) was chosen to be the new alpha infected!")

    private var targetName: String? = null
    private var prevTargetName: String? = null

    private val assassinMapHash = hashMapOf<String, String>()

    fun fetchAssassinData() {
        val handler = JSONHandler(ResourceLocation("nameless", "assassins.json"))

        for ((md5, nickname) in handler.read(JsonObject()).entrySet()) {
            assassinMapHash[md5] = nickname.asString
        }
    }

    init {

        val cBoolean = TypeRegistry.getConverterByClass(Boolean::class)
        val cChromaColor = TypeRegistry.getConverterByClass(ChromaColor::class)

        parameters["gold"] = FeatureParameter(
            0,
            "murderer",
            "glowgold",
            "Glow Gold Ingot",
            "Glow gold ingot when you are in murder mystery",
            false,
            cBoolean
        ).also {
            it.parameters["goldcolor"] = FeatureParameter(
                0,
                "murderer",
                "goldcolor",
                "Gold Ingot Color",
                "",
                Color(255, 128, 0).toChromaColor(),
                cChromaColor
            )
        }
        parameters["murderercolor"] = FeatureParameter(
            1,
            "murderer",
            "murderercolor",
            "Murderer Color",
            "",
            Color.blue.toChromaColor(),
            cChromaColor
        )

        inCategory(
            "Infection",
            "survivor" to FeatureParameter(
                0,
                "murderer",
                "glowsurvivor",
                "Glow Survivor",
                "Glow survivor in hypixel murderer INFECTION",
                true,
                cBoolean
            ).also {
                it.parameters["survivorcolor"] = FeatureParameter(
                    0,
                    "murderer",
                    "survivorcolor",
                    "Survivor Color",
                    "",
                    Color.green.toChromaColor(),
                    cChromaColor
                )
            },
            "alphacolor" to FeatureParameter(
                1,
                "murderer",
                "alphacolor",
                "Alpha Color",
                "",
                Color(128, 0, 128).toChromaColor(),
                cChromaColor
            ),
        )

        inCategory(
            "Assassin",
            "targetcolor" to FeatureParameter(
                0,
                "murderer",
                "targetcolor",
                "Target Color",
                "Target glow color in hypixel murderer ASSASSIN",
                Color.red.toChromaColor(),
                cChromaColor
            ),
            "targetarrow" to FeatureParameter(
                1,
                "murderer",
                "targetarrow",
                "Render Direction Arrow to Target",
                "Render arrow shape on your screen which is pointing the target",
                true,
                cBoolean
            ),
            "targetpath" to FeatureParameter(
                2,
                "murderer",
                "targetpath",
                "Show Paths to Target",
                "",
                false,
                cBoolean
            )
        )

    }

    override fun tick() {
        if (!checkForEnabledAndMurderMystery()) return

        val mode = Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)

        if (mode == MurdererMode.ASSASSIN) {
            if (targetName == null) {
                // Kill Contract is updated a bit later
                targetName = getTargetName(assassinMapHash)?.takeIf { it != prevTargetName }?.also {
                    sendClientMessage("§eYour new target is §c$it")
                }
            } else {
                pathTick = (pathTick + 1) % 30

                if (pathTick == 0 && getParameterValue("targetpath")) {
                    getPlayerByName(targetName)?.let { player ->
                        threadPool.execute {
                            pathsToTarget = ModPathFinding(BlockPos(player), false).findPath().get()
                        }
                    }
                }
            }
        }
    }

    /**
     * Clear all data here
     */
    override fun onServerChange(server: String) {
        murderers.clear()
        survivors.clear()
        alpha = null
        targetName = null
        pathsToTarget = emptyList()
    }

    override fun onChatReceived(e: ClientChatReceivedEvent) {
        if (!checkForEnabledAndMurderMystery()) return

        val mode = Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)
        val msg = e.message.unformattedText.trim().stripControlCodes()

        when (mode) {
            MurdererMode.INFECTION -> {
                ALPHA_FOUND.matchesMatcher(msg) {
                    alpha = it.group("alpha")
                    // this is why I set alpha to String, player may not be loaded when alpha is found
                }
                INFECTED.matchesMatcher(msg) {
                    val infectedName = it.group("infected")
                    murderers.add(infectedName)
                    survivors.remove(infectedName) // infected cannot be survivor smh

                    // same as reason why I set to String
                }
                ENVIRONMENT_INFECTED.matchesMatcher(msg) {
                    val infectedName = it.group("infected")
                    murderers.add(infectedName)
                    survivors.remove(infectedName)
                }
                ALPHA_LEFT.matchesMatcher(msg) {
                    alpha = it.group("name")
                }
            }
            MurdererMode.ASSASSIN -> {
                if (msg == "Your kill contract has been updated!") {
                    e.isCanceled = true
                    prevTargetName = targetName
                    targetName = null // reset
                }
            }
        }
    }

    override fun renderWorld(partialTicks: Float) {
        if (!checkForEnabledAndMurderMystery()) return

        if (Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE) == MurdererMode.ASSASSIN && getParameterValue("targetpath")) {
            RenderUtils.drawPath(pathsToTarget, Color.red.rgb, partialTicks)
        }
    }

    override fun onSendingPacket(e: PacketEvent.Sending) {

    }

    override fun onReceivedPacket(e: PacketEvent.Received) {
        if (!checkForEnabledAndMurderMystery()) return
        val entityPlayer: EntityPlayer
        val heldItem: ItemStack?
        when (val msg = e.packet) {
            is S04PacketEntityEquipment -> {
                entityPlayer = mc.theWorld.getEntityByID(msg.entityID) as? EntityPlayer ?: return
                heldItem = msg.itemStack
            }
            is S09PacketHeldItemChange -> {
                entityPlayer = mc.thePlayer
                heldItem = mc.thePlayer.inventory.getStackInSlot(msg.heldItemHotbarIndex)
            }
            else -> return
        }
        heldItem ?: return

        val mode = Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)

        if (mode == MurdererMode.ASSASSIN) return

        val isInfection = mode == MurdererMode.INFECTION

        if (sword_list.contains(heldItem.item)) { // found
            if (isInfection && entityPlayer.getEquipmentInSlot(3)?.item == Items.iron_chestplate) {
                alpha = entityPlayer.name
            } else {
                murderers.add(entityPlayer.name)

                if (isInfection) {
                    survivors.remove(entityPlayer.name)
                }
            }
        } else {
            if (isInfection) {
                when (heldItem.item) {
                    Items.bow -> {
                        if (heldItem.displayName.contains("§c")) { // fake bow
                            alpha = entityPlayer.name
                        } else if (heldItem.displayName.contains("§a")) {
                            survivors.add(entityPlayer.name)
                        }
                    }
                    Items.arrow -> { // there's no FAKE ARROW
                        survivors.add(entityPlayer.name)
                    }
                }
            }
        }
    }

    private fun checkForEnabledAndMurderMystery() = enabled && Hypixel.currentGame == GameType.MURDER_MYSTERY

    override fun getOutlineColor(entity: Entity): ColorInfo? {
        if (!checkForEnabledAndMurderMystery()) return null

        var colorInfo: ColorInfo? = null

        val mode = Hypixel.getProperty<MurdererMode>(PropertyKey.MURDERER_TYPE)

        if (entity is EntityPlayer) {
            val name = entity.name

            val color =
                (if (mode != MurdererMode.ASSASSIN && murderers.contains(name) && (mode != MurdererMode.INFECTION || alpha != name)) {
                    getParameterValue("murderercolor")
                } else if (mode == MurdererMode.INFECTION) {
                    if (survivors.contains(name)) {
                        if (getParameterValue("survivor")) {
                            getParameter<Boolean>("survivor").getParameterValue<Color>("survivorcolor")
                        } else return null
                    } else if (name == alpha) {
                        getParameterValue<Color>("alphacolor")
                    } else return null
                } else if (mode == MurdererMode.ASSASSIN && targetName == name.trim()) {
                    getParameterValue<Color>("targetcolor")
                } else return null).rgb

            colorInfo = colorInfo.checkAndReplace(color, ColorInfo.ColorPriority.HIGH)
        } else if (entity is EntityItem && entity.entityItem.item == Items.gold_ingot) {
            if (getParameterValue("gold")) {
                colorInfo =
                    colorInfo.checkAndReplace(
                        getParameter<Boolean>("gold").getParameterValue<Color>("goldcolor").rgb,
                        ColorInfo.ColorPriority.HIGH
                    )
            }
        }


        return colorInfo
    }

    override fun getEntityColor(entity: Entity): ColorInfo? = null

    override fun renderOverlay(partialTicks: Float) {
        if (!checkForEnabledAndMurderMystery()) return

        if (!getParameterValue<Boolean>("targetarrow")) return

        getPlayerByName(targetName)?.let {
            RenderUtils.drawDirectionArrow(it.toVec3(), Color.red.rgb)
            color(1f, 1f, 1f, 1f)

        }
    }

    //from here, assassins
    private fun getTargetName(mapHash: Map<String, String>): String? {
        val itemStack = mc.thePlayer.inventory.getStackInSlot(3) ?: return null // map which contains your target data
        val map = itemStack.item
        if (map !is ItemMap || !itemStack.displayName.contains("§c")) return null // is there a KILL CONTRACT?

        return mapHash[getNamePoints(map.getMapData(itemStack, mc.theWorld).colors).joinToString("").getMD5()]
    }

    private fun getPlayerByName(name: String?): EntityPlayer? {

        name ?: return null

        for (player in mc.theWorld.playerEntities) {
            if (player.name.trim() == name) return player
        }

        return null
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
}