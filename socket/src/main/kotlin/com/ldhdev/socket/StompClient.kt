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

import com.ldhdev.socket.chat.StompChat
import com.ldhdev.socket.data.StompPayload
import com.ldhdev.socket.subscription.StompSubscription
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import kotlin.reflect.KClass

class StompClient(serverUri: URI, val playerUUID: String, val modVersion: String) : WebSocketClient(serverUri) {

    private val listeners = mutableMapOf<KClass<out StompListener>, StompListener>()
    lateinit var uuidIdentifier: String

    val subscriptions = hashMapOf<Int, StompSubscription>()
    val receipts = hashMapOf<Int, StompPayload>()
    var receiptId = 0

    val chats = hashMapOf<String, MutableList<StompChat>>()

    fun getOrCreateChats(with: String) = chats.getOrPut(with) {
        object : ArrayList<StompChat>() {
            override fun add(element: StompChat): Boolean {

                withListener<StompListener.OnChat> { onChat(element) }

                return super.add(element)
            }
        }
    }

    fun isUUIDInitialized() = ::uuidIdentifier.isInitialized

    init {
        setListener<StompListener.OnError>(StompListener.OnError.Default)
        setListener<StompListener.OnClose>(StompListener.OnClose.Default)
        setListener<StompListener.OnReceive>(StompListener.OnReceive.Default)
        setListener<StompListener.OnOpen>(StompListener.OnOpen.Default)
        setListener<StompListener.OnSend>(StompListener.OnSend.Default)
        setListener<StompListener.OnSubscribe>(StompListener.OnSubscribe.Default)
        setListener<StompListener.OnUnsubscribe>(StompListener.OnUnsubscribe.Default)
        setListener<StompListener.OnDisconnect>(StompListener.OnDisconnect.Default)
        setListener<StompListener.OnSendChat>(StompListener.OnSendChat.Default)
        setListener<StompListener.OnMarkAsRead>(StompListener.OnMarkAsRead.Default)

    }

    fun <T : StompListener> setListener(clazz: KClass<T>, listener: StompListener) {
        listeners[clazz] = listener
    }

    inline fun <reified T : StompListener> setListener(listener: StompListener) {
        setListener(T::class, listener)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : StompListener> setListener(clazz: KClass<T>, listener: T.() -> T) {
        val existing = listeners[clazz] as T
        setListener(clazz, listener(existing))
    }

    inline fun <reified T : StompListener> setListener(noinline listener: T.() -> T) {
        setListener(T::class, listener)
    }

    internal inline fun <reified T : StompListener> getListener() = listeners[T::class] as T

    internal inline fun <reified T : StompListener> withListener(action: T.() -> Unit) = with(getListener(), action)

    override fun onOpen(handshakedata: ServerHandshake?) = withListener<StompListener.OnOpen> { onOpen() }

    override fun onMessage(message: String) {
        val payload = StompPayload.parse(message)
        withListener<StompListener.OnReceive> { onReceive(payload) }
    }

    override fun onClose(code: Int, reason: String, remote: Boolean) =
        withListener<StompListener.OnClose> { onStompClose(code, reason, remote) }

    override fun onError(ex: Exception) = withListener<StompListener.OnError> { onStompError(ex) }

    fun send(payload: StompPayload) = withListener<StompListener.OnSend> { onSend(payload) }

    fun subscribe(subscription: StompSubscription) =
        withListener<StompListener.OnSubscribe> { onSubscribe(subscription) }

    fun unsubscribe(subscription: StompSubscription) =
        withListener<StompListener.OnUnsubscribe> { onUnsubscribe(subscription) }

    fun disconnect() = withListener<StompListener.OnDisconnect> { onDisconnect() }

    fun sendChat(receiver: String, content: String) =
        withListener<StompListener.OnSendChat> { onSendChat(receiver, content) }

    fun markChatAsRead(chat: StompChat.Received) = withListener<StompListener.OnMarkAsRead> { onMarkAsRead(chat) }
}