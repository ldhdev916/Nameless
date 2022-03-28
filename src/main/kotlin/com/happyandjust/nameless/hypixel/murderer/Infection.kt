package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.core.info.ColorInfo
import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.OutlineRenderEvent
import com.happyandjust.nameless.events.PacketEvent
import com.happyandjust.nameless.features.base.ParameterHierarchy
import com.happyandjust.nameless.features.impl.qol.MurdererFinder
import com.happyandjust.nameless.features.settings
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.awt.Color

class Infection : MurdererMode {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = MurdererFinder.infection

    private var alpha: String? = null
        set(value) {
            if (field != value && value != null) {
                if (notifyAlpha && value != mc.thePlayer.name) {
                    val message = notifyMessage.replace("&", "§").replace("{name}", value)
                    sendPrefixMessage(message)
                }
                sendDebugMessage("Infection", "New alpha: $value")
            }
            field = value
        }

    private val infections = object : HashSet<String>() {
        override fun add(element: String): Boolean {
            survivors.remove(element)

            val newlyAdded = super.add(element)

            if (newlyAdded) {
                sendDebugMessage("Infection", "New infection: $element")
            }

            return newlyAdded
        }
    }

    private val survivors = object : HashSet<String>() {
        override fun add(element: String): Boolean {
            val newlyAdded = super.add(element)

            sendDebugMessage("Infection", "New survivor: $element")

            return newlyAdded
        }
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")
    override fun registerEventListeners() {
        on<ClientChatReceivedEvent>().addSubscribe {
            val pureText = pureText
            ALPHA_FOUND.matchesMatcher(pureText) {
                alpha = group("alpha")
                return@addSubscribe
            }
            INFECTED.matchesMatcher(pureText) {
                infections.add(group("infected"))
                return@addSubscribe
            }
            ENVIRONMENT_INFECTED.matchesMatcher(pureText) {
                infections.add(group("infected"))
                return@addSubscribe
            }

            ALPHA_LEFT.matchesMatcher(pureText) {
                alpha = group("name")
                return@addSubscribe
            }
        }

        on<PacketEvent.Received>().addSubscribe {
            val (entityPlayer, heldItem) = PlayerHeldItemFactory.createImpl(packet)
            entityPlayer ?: return@addSubscribe
            heldItem ?: return@addSubscribe

            val playerName = entityPlayer.name
            when (heldItem.item) {
                in MurdererFinder.sword_list -> {
                    val chestPlate = entityPlayer.getEquipmentInSlot(3).item

                    if (chestPlate == Items.iron_chestplate) {
                        alpha = playerName
                    } else {
                        infections.add(playerName)
                    }
                }
                Items.bow -> {
                    val displayName = heldItem.displayName
                    when {
                        "§c" in displayName -> alpha = playerName
                        "§a" in displayName -> survivors.add(playerName)
                    }
                }
                Items.arrow -> survivors.add(playerName)
            }
        }

        on<OutlineRenderEvent>().filter { entity is EntityPlayer }.addSubscribe {
            val name = entity.name
            val color = when {
                name == alpha -> alphaColor
                showInfection && name in infections -> infectionColor
                showSurvivor && name in survivors -> survivorColor
                else -> return@addSubscribe
            }.rgb

            colorInfo = ColorInfo(color, ColorInfo.ColorPriority.HIGH)
        }
    }

    companion object : MurdererModeCreator {
        override fun createImpl() = Infection()

        override val modes = setOf("MURDER_INFECTION")

        private val ALPHA_FOUND = "The alpha, (?<alpha>\\w+), has been revealed by \\w+!".toPattern()
        private val INFECTED = "\\w+(\\s\\w+)? infected (?<infected>\\w+)".toPattern()
        private val ENVIRONMENT_INFECTED = "(?<infected>\\w+) was infected by the environment!".toPattern()
        private val ALPHA_LEFT =
            "The alpha left the game\\. (?<name>\\w+) was chosen to be the new alpha infected!".toPattern()

        private var alphaColor by parameter(Color(128, 0, 128).toChromaColor()) {
            key = "alphaColor"
            title = "Alpha Color"
        }

        private var notifyAlpha by parameter(true) {
            key = "notifyAlpha"
            title = "Notify Alpha in Chat"

            settings {
                ordinal = 1
            }
        }

        private var notifyMessage by parameter("&cAlpha: {name}") {
            key = "notifyMessage"
            title = "Notify Message"
        }

        private var showInfection by parameter(true) {
            key = "showInfection"
            title = "Render Outline on Infections"

            settings {
                ordinal = 2
            }
        }

        private var infectionColor by parameter(Color.blue.toChromaColor()) {
            key = "infectionColor"
            title = "Infection Color"
        }

        private var showSurvivor by parameter(true) {
            key = "showSurvivor"
            title = "Render Outline on Survivors"

            settings {
                ordinal = 3
            }
        }

        private var survivorColor by parameter(Color.green.toChromaColor()) {
            key = "survivorColor"
            title = "Survivor Color"
        }

        override fun ParameterHierarchy.setupHierarchy() {
            +::alphaColor

            ::notifyAlpha {
                +::notifyMessage
            }

            ::showInfection {
                +::infectionColor
            }

            ::showSurvivor {
                +::survivorColor
            }
        }
    }
}