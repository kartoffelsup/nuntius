package io.github.kartoffelsup.nuntius.events

import arrow.Kind
import arrow.fx.Queue
import arrow.fx.typeclasses.Concurrent
import kotlin.reflect.KClass

fun <F> NuntiusEventBus(CF: Concurrent<F>): Kind<F, NuntiusEventBus<F>> = CF.run {
    Ref(emptyMap<KClass<*>, List<Queue<F, Any>>>()).map { ref ->
        object : NuntiusEventBus<F> {
            override fun <T : Any> send(event: T): Kind<F, Unit> =
                ref.get().map { it[event::class] }.flatMap { qs ->
                    qs?.map { queue ->
                        queue.offer(event)
                    }?.parSequence()?.unit() ?: unit()
                }

            override fun <T : Any> listen(type: KClass<T>): Kind<F, T> =
                Queue.bounded<F, Any>(1, CF).flatMap { q ->
                    ref.update { types -> types.update(type) { it + q } }
                        .followedBy(q.take().map { event -> event as T })
                }
        }
    }
}

private fun <F> Map<KClass<*>, List<Queue<F, Any>>>.update(
    key: KClass<*>,
    update: (List<Queue<F, Any>>) -> List<Queue<F, Any>>
): Map<KClass<*>, List<Queue<F, Any>>> {
    val original = get(key) ?: emptyList()
    val new = update(original)
    return this + Pair(key, new)
}
