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

package com.happyandjust.nameless.dsl

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.reflect.KClass

private val createdHandlers = hashMapOf<KClass<out Event>, Handler<out Event>>()

class SubscriptionBuilder<T : Event>(private val eventClass: KClass<T>) {

    private var subscribed = false
    private var filters = arrayListOf<(T) -> Boolean>()
    private var priority = EventPriority.NORMAL
    private var receiveCanceled = false

    fun filter(predicate: (T) -> Boolean) = apply {
        filters.add(predicate)
    }

    fun setPriority(eventPriority: EventPriority) = apply {
        priority = eventPriority
    }

    fun setReceiveCanceled(receiveCanceled: Boolean) = apply {
        this.receiveCanceled = receiveCanceled
    }

    fun subscribe(action: (T) -> Unit) {
        if (subscribed) return
        val handler = createdHandlers.getOrPut(eventClass) { Handler(eventClass.java) } as Handler<T>

        handler.addListener(
            handler.HandlerData(
                action,
                { filters.all { filter -> filter(it) } },
                priority,
                receiveCanceled
            )
        )
        subscribed = true
    }

}

class Handler<T : Event>(val eventClass: Class<T>) {

    inner class HandlerData(
        val action: (T) -> Unit,
        val filter: (T) -> Boolean,
        val priority: EventPriority,
        val receiveCanceled: Boolean
    )

    private val listeners = arrayListOf<HandlerData>()

    fun addListener(handlerData: HandlerData) {
        listeners.add(handlerData)
        listeners.sortByDescending { it.priority }
    }


    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun handle(e: T) {
        if (e.javaClass != eventClass) return
        for (listener in listeners) {
            if (listener.filter(e) && (!e.isCanceled || listener.receiveCanceled)) {
                listener.action(e)
            }
        }
    }
}

inline fun <reified T : Event> on() = SubscriptionBuilder(T::class)