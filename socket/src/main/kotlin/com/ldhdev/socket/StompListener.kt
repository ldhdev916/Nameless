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

package com.ldhdev.socket

import com.ldhdev.namelessstd.*
import com.ldhdev.socket.chat.StompChat
import com.ldhdev.socket.chat.StompChatData
import com.ldhdev.socket.data.*
import com.ldhdev.socket.subscription.StompMessageHandler
import com.ldhdev.socket.subscription.StompSubscription
import java.time.LocalDateTime
import java.util.*

@Target(AnnotationTarget.CLASS)
private annotation class ShouldBeProvided

sealed interface StompListener {

    fun interface OnError : StompListener {
        fun StompClient.onStompError(e: Exception)

        object Default : OnError {
            override fun StompClient.onStompError(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun interface OnClose : StompListener {
        fun StompClient.onStompClose(code: Int, reason: String, remote: Boolean)

        object Default : OnClose {
            override fun StompClient.onStompClose(code: Int, reason: String, remote: Boolean) = Unit
        }
    }

    fun interface OnReceive : StompListener {
        fun StompClient.onReceive(payload: StompPayload)

        object Default : OnReceive {

            override fun StompClient.onReceive(payload: StompPayload) {
                when (payload.method) {
                    StompMethod.CONNECTED -> {
                        var joinSubscription: StompSubscription? = null
                        val handler = StompMessageHandler {
                            uuidIdentifier = it.payload!!
                            unsubscribe(joinSubscription!!)

                            withListener<OnModIdSetup> { onModIdSetup() }
                        }

                        subscribe(
                            StompSubscription(
                                Route.Client.SendId.withVariables(Variable.Id to playerUUID),
                                handler
                            ).also { joinSubscription = it }
                        )
                        send(
                            StompSend(Route.Server.Join).header(
                                Headers.PlayerUUID to playerUUID,
                                Headers.ModVersion to modVersion
                            )
                        )
                    }
                    StompMethod.ERROR -> close()
                    StompMethod.RECEIPT -> {
                        val receiptId = payload.headers["receipt-id"]!!.toInt()
                        val receiptPayload = receipts.remove(receiptId)!!
                        if (receiptPayload.method == StompMethod.DISCONNECT) close()
                    }
                    StompMethod.MESSAGE -> {
                        val subscription = subscriptions[payload.headers["subscription"]!!.toInt()]!!
                        with(subscription.handler) {
                            handle(payload)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun interface OnOpen : StompListener {
        fun StompClient.onOpen()

        object Default : OnOpen {
            override fun StompClient.onOpen() {
                send(StompPayload(StompMethod.CONNECT).header("accept-version" to "1.2", "host" to uri.host))
            }
        }
    }

    fun interface OnSend : StompListener {
        fun StompClient.onSend(payload: StompPayload)

        object Default : OnSend {
            override fun StompClient.onSend(payload: StompPayload) {
                if (isUUIDInitialized()) payload.header(Headers.ModId to uuidIdentifier)
                send(payload.getMessage())
            }
        }
    }

    fun interface OnSubscribe : StompListener {
        fun StompClient.onSubscribe(subscription: StompSubscription)

        object Default : OnSubscribe {

            private var increasingId = 0

            override fun StompClient.onSubscribe(subscription: StompSubscription) {
                subscription.id = increasingId++
                subscriptions[subscription.id] = subscription
                send(
                    StompPayload(StompMethod.SUBSCRIBE).header(
                        "destination" to subscription.destination.client,
                        "id" to subscription.id
                    )
                )
            }
        }
    }

    fun interface OnUnsubscribe : StompListener {
        fun StompClient.onUnsubscribe(subscription: StompSubscription)

        object Default : OnUnsubscribe {
            override fun StompClient.onUnsubscribe(subscription: StompSubscription) {
                send(StompPayload(StompMethod.UNSUBSCRIBE).header("id" to subscription.id))
                subscriptions.remove(subscription.id)
            }
        }
    }

    fun interface OnDisconnect : StompListener {
        fun StompClient.onDisconnect()

        object Default : OnDisconnect {
            override fun StompClient.onDisconnect() {
                send(StompSend(Route.Server.Disconnect))

                val id = receiptId++
                val payload = StompPayload(StompMethod.DISCONNECT).header("receipt" to id)
                receipts[id] = payload
                send(payload)
            }
        }
    }

    @ShouldBeProvided
    fun interface OnPosition : StompListener {
        fun StompClient.getPosition(): Triple<Number, Number, Number>?
    }

    @ShouldBeProvided
    fun interface OnChat : StompListener {
        fun StompClient.onChat(chat: StompChat)
    }

    @ShouldBeProvided
    fun interface OnRead : StompListener {
        fun StompClient.onRead(chat: StompChat.Sending)
    }

    fun interface OnSendChat : StompListener {
        fun StompClient.onSendChat(receiver: String, content: String)

        object Default : OnSendChat {
            override fun StompClient.onSendChat(receiver: String, content: String) {
                val data = StompChatData(content, LocalDateTime.now(), UUID.randomUUID().toString())
                val chat = StompChat.Sending(data, receiver)
                send(StompSendChat(chat))
                getOrCreateChats(receiver).add(chat)
            }
        }
    }

    fun interface OnMarkAsRead : StompListener {
        fun StompClient.onMarkAsRead(chat: StompChat.Received)

        object Default : OnMarkAsRead {
            override fun StompClient.onMarkAsRead(chat: StompChat.Received) {
                if (chat.markedAsRead) return
                chat.markedAsRead = true
                send(StompMarkAsRead(chat))
            }
        }
    }

    fun interface OnModIdSetup : StompListener {
        fun StompClient.onModIdSetup()

        object Default : OnModIdSetup {
            override fun StompClient.onModIdSetup() {
                subscribeWithId(Route.Client.Chat, chatHandler)
                subscribeWithId(Route.Client.Position, positionHandler)
                subscribeWithId(Route.Client.NotifyRead, readHandler)
            }
        }
    }
}