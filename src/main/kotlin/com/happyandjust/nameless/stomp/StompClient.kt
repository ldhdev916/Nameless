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

package com.happyandjust.nameless.stomp

import com.happyandjust.nameless.VERSION
import com.happyandjust.nameless.dsl.sendDebugMessage
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.time.LocalDateTime

class StompClient(serverUri: URI, private val playerUUID: String) : WebSocketClient(serverUri), StompInterface {

    private var internalId = 0
    private val subscriptions = hashMapOf<Int, StompSubscription>()
    private val receipts = hashMapOf<Int, StompPayload>()
    lateinit var uuidIdentifier: String
    val chatsByPlayer = hashMapOf<String, ObservableChatList>()

    init {
        connectBlocking()
        Runtime.getRuntime().addShutdownHook(Thread {
            disconnect()
        })
    }

    private var status = StompClientStatus.CONNECTING
        set(value) {
            if (field != value) {
                field = value
                sendDebugMessage("New stomp status: $value")
            }
        }

    override fun onOpen(handshakedata: ServerHandshake) {
        send(StompPayload(StompHeader.CONNECT).header("accept-version" to "1.2", "host" to uri.host))
    }

    private fun subscribeDefaults() {
        var subscription: StompSubscription? = null
        val handler = StompMessageHandler {
            uuidIdentifier = it.payload!!
            unsubscribe(subscription!!)

            subscribe(StompSubscription("/topic/chat/$uuidIdentifier") { chatPayload ->
                val sender = chatPayload.headers.getOrDefault("sender", "Unknown")
                val chat = StompChat(chatPayload.payload!!, LocalDateTime.now(), true)

                chatsByPlayer.getOrPut(sender) { ObservableChatList() }.add(chat)
            })
        }
        subscribe(StompSubscription("/topic/$playerUUID", handler).also { subscription = it })
        send(StompPayload().header("destination" to "/mod/join", "uuid" to playerUUID))
    }

    override fun onMessage(message: String) {
        val payload = StompPayload.parse(message)
        sendDebugMessage("Received $payload")
        when (payload.method) {
            StompHeader.CONNECTED -> {
                status = StompClientStatus.CONNECTED
                subscribeDefaults()
            }
            StompHeader.ERROR -> {
                status = StompClientStatus.ERROR
                close()
            }
            StompHeader.RECEIPT -> {
                val receiptId = payload.headers["receipt-id"]!!.toInt()
                val receiptPayload = receipts.remove(receiptId)!!
                if (receiptPayload.method == StompHeader.DISCONNECT) {
                    status = StompClientStatus.DISCONNECTED
                    close()
                }
            }
            StompHeader.MESSAGE -> {
                val subscription = subscriptions[payload.headers["subscription"]!!.toInt()]!!
                with(subscription.handler) {
                    handle(payload)
                }
            }
            else -> {
                sendDebugMessage(payload.method)
            }
        }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) {
        sendDebugMessage("Stomp Closed code=$code, reason=$reason, remote=$remote")
    }

    override fun onError(ex: Exception) {
        ex.printStackTrace()
    }

    override fun send(payload: StompPayload) {
        val realPayload = if (::uuidIdentifier.isInitialized) {
            payload.header("mod-uuid" to uuidIdentifier)
        } else {
            payload.header("mod-version" to VERSION)
        }
        sendDebugMessage("Sending $realPayload")
        send(realPayload.getMessage())
    }

    override fun subscribe(subscription: StompSubscription) {
        checkConnected()
        subscription.id = internalId++
        subscriptions[subscription.id] = subscription
        send(
            StompPayload(StompHeader.SUBSCRIBE).header(
                "destination" to subscription.destination,
                "id" to subscription.id
            )
        )
    }

    override fun unsubscribe(subscription: StompSubscription) {
        checkConnected()
        send(StompPayload(StompHeader.UNSUBSCRIBE).header("id" to subscription.id))
        subscriptions.remove(subscription.id)
    }

    override fun sendChat(receiver: String, content: String) {
        val chat = StompChat(content, LocalDateTime.now(), false)

        send(StompPayload().header("destination" to "/mod/chat/$receiver").payload(content))
        chatsByPlayer.getOrPut(receiver) { ObservableChatList() }.add(chat)
    }

    override fun disconnect() {
        checkConnected()

        status = StompClientStatus.DISCONNECTING

        send(StompPayload().header("destination" to "/mod/disconnect"))

        val receiptId = internalId++
        val payload = StompPayload(StompHeader.DISCONNECT).header("receipt" to receiptId)
        receipts[receiptId] = payload
        send(payload)
    }

    private fun checkConnected() {
        check(status == StompClientStatus.CONNECTED) { "Current stomp status: $status" }
    }
}