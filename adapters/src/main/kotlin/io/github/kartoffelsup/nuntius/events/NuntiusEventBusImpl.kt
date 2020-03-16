package io.github.kartoffelsup.nuntius.events

import java.util.Collections
import kotlin.reflect.KClass

private class Event(val type: KClass<*>, val event: Any)

class NuntiusEventBusImpl() : NuntiusEventBus {
    private val queue: MutableList<Event> = mutableListOf()
    private val listeners: MutableMap<KClass<*>, MutableList<EventListener<Any>>> =
        Collections.synchronizedMap(mutableMapOf())

    override fun <T : Any> send(event: T) {
        queue.add(Event(event::class, event))
        processQueue()
    }

    override fun <T : Any> listen(type: KClass<T>, listener: EventListener<T>) {
        listeners.compute(type) { t: KClass<*>, u: MutableList<EventListener<Any>>? ->
            val l = u ?: mutableListOf()
            (listener as? EventListener<Any>)?. let { l.add(it) }
            l
        }
        processQueue()
    }

    private fun processQueue() {
       queue.removeAll { event ->
           val found = listeners.filter {
               it.key == event.type
           }
           found.forEach { (_, u) ->
               u.forEach { it(event.event) }
           }
           found.isNotEmpty()
       }
    }
}
