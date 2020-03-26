package io.github.kartoffelsup.nuntius.events

import arrow.Kind
import kotlin.reflect.KClass

interface NuntiusEventBus<F> {
    fun <T : Any> send(event: T): Kind<F, Unit>
    fun <T : Any> listen(type: KClass<T>): Kind<F, T>
}
