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

package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.input.InputPlaceHolder
import com.happyandjust.nameless.core.input.buildComposite
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.SpecialOverlayEvent
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.ParameterHierarchy
import com.happyandjust.nameless.features.impl.qol.MurdererFinder
import com.happyandjust.nameless.features.settings
import com.happyandjust.nameless.pathfinding.handler.CachePathHandler
import com.happyandjust.nameless.pathfinding.handler.PathHandler
import com.happyandjust.nameless.pathfinding.handler.PathHandlerImpl
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.item.ItemMap
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

class Assassins : MurdererMode {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = MurdererFinder.assassins

    private var targetName: String? = null
    private var prevTargetName: String? = null

    private val handler: PathHandler by lazy { CachePathHandler(PathHandlerImpl(false), 1.5) }

    private fun getTargetPlayer() = mc.theWorld.playerEntities.find { it.name == targetName }

    override fun registerEventListeners() {
        on<ClientChatReceivedEvent>().filter { pureText == "Your kill contract has been updated!" }.addSubscribe {
            if (cancelContractMessage) cancel()
            prevTargetName = targetName
            targetName = null
        }

        var prevColors: ByteArray? = null
        on<SpecialTickEvent>().addSubscribe {
            val itemStack = mc.thePlayer.inventory.getStackInSlot(3)
            val map = itemStack?.item
            if (map !is ItemMap || !itemStack.displayName.contains("§c")) return@addSubscribe
            val colors = map.getMapData(itemStack, mc.theWorld).colors

            if (!prevColors.contentEquals(colors)) {
                prevColors = colors
                val foundTarget = assassinsHash.getOrElse(colors.getMD5()) {
                    sendDebugMessage("Assassins", "§cUnknown Hash: ${colors.getMD5()}")
                    return@addSubscribe
                }
                if (foundTarget != targetName) {
                    sendClientMessage(notifyMessage.asString("name" to foundTarget.trim()))
                    targetName = foundTarget
                }
            }
        }

        on<OutlineRenderEvent>().filter { entity == getTargetPlayer() }.addSubscribe {
            colorInfo = ColorInfo(targetColor.rgb, ColorInfo.ColorPriority.HIGHEST)
        }

        on<RenderWorldLastEvent>().filter { targetPath }.addSubscribe {
            val targetPlayer = getTargetPlayer() ?: return@addSubscribe
            handler.getPath(BlockPos(targetPlayer)).drawPaths(Color.red.rgb, partialTicks)
        }

        on<SpecialOverlayEvent>().filter { targetArrow }.addSubscribe {
            val targetPlayer = getTargetPlayer() ?: return@addSubscribe
            targetPlayer.toVec3().drawDirectionArrow(Color.red.rgb)
        }
    }

    companion object : MurdererModeCreator {

        private const val UPDATE = "Your kill contract has been updated!"

        @OptIn(ExperimentalSerializationApi::class)
        private val assassinsHash: Map<String, String> =
            ResourceLocation("nameless", "assassins.json").inputStream().buffered().use(Json::decodeFromStream)


        override fun createImpl() = Assassins()

        override val modes = setOf("MURDER_ASSASSINS")

        private var notifyTarget by parameter(true) {
            key = "notifyTarget"
            title = "Notify Target in Chat"
        }

        private var notifyMessage by userInputParameter(buildComposite {
            color { EnumChatFormatting.YELLOW }
            text { "Your new target is " }
            color { EnumChatFormatting.RED }
            value { "name" }
        }) {
            key = "notifyMessage"
            title = "Notify Message"

            settings {
                registeredPlaceHolders = listOf(InputPlaceHolder("name", "Target player's name"))
            }
        }

        private var cancelContractMessage by parameter(true) {
            key = "cancelContract"
            title = "Cancel Contract Update Message"
            desc = "Cancel '$UPDATE'"
        }

        private var targetColor by parameter(Color.red.toChromaColor()) {
            key = "targetColor"
            title = "Target Outline Color"
        }

        private var targetArrow by parameter(true) {
            key = "targetArrow"
            title = "Render Direction Arrow to Target"
            desc = "Render arrow on your screen pointing to the target"
        }

        private var targetPath by parameter(false) {
            key = "targetPath"
            title = "Show Paths to Target"
        }

        override fun ParameterHierarchy.setupHierarchy() {
            ::notifyTarget {
                +::notifyMessage
            }

            +::cancelContractMessage

            +::targetColor

            +::targetArrow

            +::targetPath
        }
    }
}