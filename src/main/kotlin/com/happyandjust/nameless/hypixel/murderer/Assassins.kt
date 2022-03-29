package com.happyandjust.nameless.hypixel.murderer

import com.happyandjust.nameless.core.value.toChromaColor
import com.happyandjust.nameless.dsl.*
import com.happyandjust.nameless.events.SpecialTickEvent
import com.happyandjust.nameless.features.base.ParameterHierarchy
import com.happyandjust.nameless.features.impl.qol.MurdererFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.item.ItemMap
import net.minecraftforge.client.event.ClientChatReceivedEvent
import java.awt.Color
import java.net.HttpURLConnection
import java.net.URL

class Assassins : MurdererMode {
    override val unregisterEventCallbacks: MutableSet<() -> Unit> = hashSetOf()

    override fun isEnabled() = MurdererFinder.assassins

    private var targetName: String? = null
    private var prevTargetName: String? = null

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
                sendDebugMessage("Assassins", "Sending color data to server")

                CoroutineScope(Dispatchers.IO).launch {
                    with(URL("http://3.37.56.106:8080/assassins").openConnection() as HttpURLConnection) {
                        requestMethod = "POST"

                        doOutput = true

                        outputStream.buffered().use {
                            it.write(colors)
                        }

                        sendDebugMessage(
                            "Assassins",
                            "Response Code: $responseCode, Response: ${inputStream.readBytes().decodeToString()}"
                        )
                    }
                }
            }
        }
    }

    companion object : MurdererModeCreator {

        private const val UPDATE = "Your kill contract has been updated!"

        override fun createImpl() = Assassins()

        override val modes = setOf("MURDER_ASSASSINS")

        private var notifyTarget by parameter(true) {
            key = "notifyTarget"
            title = "Notify Target in Chat"
        }

        private var notifyMessage by parameter("&eYour new target is &c{name}") {
            key = "notifyMessage"
            title = "Notify Message"
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