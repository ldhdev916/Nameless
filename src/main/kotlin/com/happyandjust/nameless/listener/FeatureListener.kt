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

package com.happyandjust.nameless.listener

import com.happyandjust.nameless.dsl.color
import com.happyandjust.nameless.dsl.mc
import com.happyandjust.nameless.events.FeatureStateChangeEvent
import com.happyandjust.nameless.events.HypixelServerChangeEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.events.PartyGameChangeEvent
import com.happyandjust.nameless.features.FeatureRegistry
import com.happyandjust.nameless.features.listener.*
import net.minecraft.entity.EntityLivingBase
import net.minecraftforge.client.event.*
import net.minecraftforge.client.event.sound.PlaySoundEvent
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent
import net.minecraftforge.event.entity.living.EnderTeleportEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object FeatureListener {

    private inline fun <reified T> invoke(block: T.() -> Unit) {
        for (feature in FeatureRegistry.features) {
            if (feature is T) {
                feature.block()
            }
            if (feature.enabled) {
                for ((processor, shouldExecute) in feature.processors) {
                    if (processor is T && shouldExecute()) {
                        processor.block()
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onPartyGame(e: PartyGameChangeEvent) {
        invoke<PartyGameChangeListener> { onPartyGameChange(e.from, e.to) }
    }

    @SubscribeEvent
    fun onClientTick(e: TickEvent.ClientTickEvent) {
        if (e.phase == TickEvent.Phase.START) return
        if (mc.theWorld == null || mc.thePlayer == null) {
            invoke<ClientTickListener> { tickWorldNull() }
            return
        }
        invoke<ClientTickListener> { tick() }
    }

    @SubscribeEvent
    fun featureStateChangePre(e: FeatureStateChangeEvent.Pre) {
        invoke<FeatureStateListener> { onFeatureStateChangePre(e) }
    }

    @SubscribeEvent
    fun featureStateChangePost(e: FeatureStateChangeEvent.Post) {
        invoke<FeatureStateListener> { onFeatureStateChangePost(e) }
    }

    @SubscribeEvent
    fun onRenderGameOverlay(e: RenderGameOverlayEvent) {
        if (e.type == RenderGameOverlayEvent.ElementType.BOSSHEALTH) {
            invoke<RenderOverlayListener> { renderOverlay(e.partialTicks) }
        }
        color(1f, 1f, 1f, 1f)
    }

    @SubscribeEvent
    fun onServerChange(e: HypixelServerChangeEvent) {
        invoke<ServerChangeListener> { onServerChange(e.server) }
    }

    @SubscribeEvent
    fun onRenderWorldLast(e: RenderWorldLastEvent) {
        invoke<WorldRenderListener> { renderWorld(e.partialTicks) }
    }

    @SubscribeEvent
    fun onKeyInput(e: InputEvent.KeyInputEvent) {
        invoke<KeyInputListener> { onKeyInput() }
    }

    @SubscribeEvent
    fun onActionPerformedPre(e: GuiScreenEvent.ActionPerformedEvent.Pre) {
        invoke<ScreenActionPerformedListener> { actionPerformedPre(e) }
    }

    @SubscribeEvent
    fun onActionPerformedPost(e: GuiScreenEvent.ActionPerformedEvent.Post) {
        invoke<ScreenActionPerformedListener> { actionPerformedPost(e) }
    }

    @SubscribeEvent
    fun onScreenInitPre(e: GuiScreenEvent.InitGuiEvent.Pre) {
        invoke<ScreenInitListener> { screenInitPre(e) }
    }

    @SubscribeEvent
    fun onScreenInitPost(e: GuiScreenEvent.InitGuiEvent.Post) {
        invoke<ScreenInitListener> { screenInitPost(e) }
    }

    @SubscribeEvent
    fun sendingPacket(e: PacketEvent.Sending) {
        invoke<PacketListener> { onSendingPacket(e) }
    }

    @SubscribeEvent
    fun receivedPacket(e: PacketEvent.Received) {
        invoke<PacketListener> { onReceivedPacket(e) }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    fun onChat(e: ClientChatReceivedEvent) {
        invoke<ChatListener> { onChatReceived(e) }
    }

    @SubscribeEvent
    fun itemTooltip(e: ItemTooltipEvent) {
        invoke<ItemTooltipListener> { onItemTooltip(e) }
    }

    @SubscribeEvent
    fun playerPre(e: RenderPlayerEvent.Pre) {
        invoke<RenderPlayerListener> { onRenderPlayerPre(e) }
    }

    @SubscribeEvent
    fun playerPost(e: RenderPlayerEvent.Post) {
        invoke<RenderPlayerListener> { onRenderPlayerPost(e) }
    }

    @SubscribeEvent
    fun backgroundDrawn(e: GuiScreenEvent.BackgroundDrawnEvent) {
        invoke<BackgroundDrawnListener> { onBackgroundDrawn(e) }
    }

    @SubscribeEvent
    fun playSound(e: PlaySoundEvent) {
        invoke<PlaySoundListener> { onPlaySound(e) }
    }

    @SubscribeEvent
    fun playSoundAtEntity(e: PlaySoundAtEntityEvent) {
        invoke<PlaySoundListener> { onPlaySoundAtEntity(e) }
    }

    @SubscribeEvent
    fun worldLoad(e: WorldEvent.Load) {
        invoke<WorldLoadListener> { onWorldLoad(e) }
    }

    @SubscribeEvent
    fun renderLivingPre(e: RenderLivingEvent.Pre<EntityLivingBase>) {
        invoke<RenderLivingListener> { onRenderLivingPre(e) }
    }

    @SubscribeEvent
    fun renderLivingPost(e: RenderLivingEvent.Post<EntityLivingBase>) {
        invoke<RenderLivingListener> { onRenderLivingPost(e) }
    }

    @SubscribeEvent
    fun guiOpen(e: GuiOpenEvent) {
        invoke<ScreenOpenListener> { onGuiOpen(e) }
    }

    @SubscribeEvent
    fun enderTeleport(e: EnderTeleportEvent) {
        invoke<EnderTeleportListener> { onEnderTeleport(e) }
    }

    @SubscribeEvent
    fun mouseInputPre(e: GuiScreenEvent.MouseInputEvent.Pre) {
        invoke<ScreenMouseInputListener> { onMouseInputPre(e) }
    }

    @SubscribeEvent
    fun mouseInputPost(e: GuiScreenEvent.MouseInputEvent.Post) {
        invoke<ScreenMouseInputListener> { onMouseInputPost(e) }
    }

}