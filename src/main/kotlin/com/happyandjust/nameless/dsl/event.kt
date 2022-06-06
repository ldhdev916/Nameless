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

package com.happyandjust.nameless.dsl

import com.happyandjust.nameless.Nameless
import com.happyandjust.nameless.core.TickTimer
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.EventPriority
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes.*
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KClass

private val createdHandlers = hashMapOf<KClass<out Event>, Handler<out Event>>()
private val classLoader =
    ASMEventHandler::class.java.getDeclaredField("LOADER").apply { isAccessible = true }[null] as ClassLoader

private val defineClassMethod = ClassLoader::class.java.getDeclaredMethod(
    "defineClass",
    String::class.java,
    ByteArray::class.java,
    Int::class.java,
    Int::class.java
).apply { isAccessible = true }

class SubscriptionBuilder<T : Event>(private val eventClass: KClass<T>) {

    private var subscribed = false
    private var filters = arrayListOf<T.() -> Boolean>()
    var priority = EventPriority.NORMAL
    var receiveCanceled = true

    fun filter(predicate: T.() -> Boolean) = apply {
        filters.add(predicate)
    }

    fun timerFilter(timer: TickTimer) = filter { timer.update().check() }

    /**
     * @return Callback that unregisters listener
     */
    @Suppress("UNCHECKED_CAST")
    fun subscribe(action: T.() -> Unit): () -> Unit {
        if (subscribed) error("Already subscribed")
        val handler = createdHandlers.getOrPut(eventClass) { Handler(eventClass.java) } as Handler<T>

        val handlerData = handler.HandlerData(
            action,
            { filters.all { it() } },
            priority,
            receiveCanceled
        )
        handler.addListener(handlerData)
        subscribed = true

        return { handler.removeListener(handlerData) }
    }

}

class Handler<T : Event>(private val eventClass: Class<T>) {

    inner class HandlerData(
        val action: T.() -> Unit,
        val filter: T.() -> Boolean,
        val priority: EventPriority,
        val receiveCanceled: Boolean
    )

    private val listeners = CopyOnWriteArrayList<HandlerData>()

    fun addListener(handlerData: HandlerData) {
        listeners.add(handlerData)
        listeners.sortBy { it.priority }
    }

    fun removeListener(handlerData: HandlerData) {
        listeners.remove(handlerData)
    }

    private fun getHandlerClass(): Class<*> {
        /*
        public class Handler_SomeEvent {

            private final Handler handler;

            public Handler_SomeEvent(Handler var1) {
                this.handler = var1;
            }

            @SubscribeEvent(receiveCanceled = true, priority = EventPriority.HIGHEST)
            public void handle_SomeEvent(SomeEvent var1) {
                this.handler.handleEvent(var1);
            }

        }
         */
        val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
        val eventName = eventClass.name.split(".").last().replace("$", "_")
        val fullEventName = eventClass.name.replace(".", "/")
        val handler = "com/happyandjust/nameless/dsl/Handler"

        val name = "com/happyandjust/nameless/Handler_$eventName"

        cw.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", null)

        cw.visitField(ACC_PRIVATE + ACC_FINAL, "handler", "L$handler;", null, null)

        with(cw.visitMethod(ACC_PUBLIC, "<init>", "(L$handler;)V", null, null)) {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)

            visitVarInsn(ALOAD, 0)
            visitVarInsn(ALOAD, 1)
            visitFieldInsn(PUTFIELD, name, "handler", "L$handler;")

            visitInsn(RETURN)
            visitEnd()
            visitMaxs(0, 0)
        }

        with(cw.visitMethod(ACC_PUBLIC, "handle_$eventName", "(L$fullEventName;)V", null, null)) {
            visitAnnotation("Lnet/minecraftforge/fml/common/eventhandler/SubscribeEvent;", true).apply {
                visit("receiveCanceled", true)
                visitEnum("priority", "Lnet/minecraftforge/fml/common/eventhandler/EventPriority;", "HIGHEST")
            }
            visitCode()

            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, name, "handler", "L$handler;")
            visitVarInsn(ALOAD, 1)
            visitMethodInsn(
                INVOKEVIRTUAL,
                handler,
                "handleEvent",
                "(Lnet/minecraftforge/fml/common/eventhandler/Event;)V",
                false
            )

            visitInsn(RETURN)
            visitEnd()
            visitMaxs(0, 0)
        }
        val classBytes = cw.toByteArray()
        return defineClassMethod(classLoader, name.replace("/", "."), classBytes, 0, classBytes.size) as Class<*>
    }


    init {
        val eventListener = getHandlerClass().getConstructor(javaClass).newInstance(this)
        Nameless.requestRegisterEventHandler(eventListener)
    }

    @Suppress("UNUSED")
    fun handleEvent(e: T) {
        for (listener in listeners) {
            if (listener.filter(e) && (!e.isCanceled || listener.receiveCanceled)) {
                listener.action(e)
            }
        }
    }
}

inline fun <reified T : Event> on() = SubscriptionBuilder(T::class)

val ClientChatReceivedEvent.pureText
    get() = message.unformattedText.trim().stripControlCodes()

fun Event.cancel() = apply {
    isCanceled = true
}

interface TempEventListener {
    val unregisterEventCallbacks: MutableSet<() -> Unit>

    fun registerEventListeners()

    fun <T : Event> SubscriptionBuilder<T>.addSubscribe(action: T.() -> Unit) {
        unregisterEventCallbacks.add(subscribe(action))
    }

    fun unregisterAll() {
        sendDebugMessage("TempEventListener", "Unregistering ${javaClass.name}")
        unregisterEventCallbacks.forEach { it() }
    }
}