package io.github.kartoffelsup.nuntius.events

import kotlin.reflect.KClass

typealias EventListener<T> = (T) -> Unit

interface NuntiusEventBus {
    fun <T : Any> send(event: T)
    fun <T : Any> listen(type: KClass<T>, listener: EventListener<T>)
}
