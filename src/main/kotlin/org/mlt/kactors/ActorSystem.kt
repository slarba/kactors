package org.mlt.kactors

import java.util.concurrent.atomic.AtomicInteger

class ActorSystem : ActorContext {
    private val nextActorId = AtomicInteger()
    private val scheduler = QueuePerThreadScheduler(32)

    override fun <T> actorOf(name: String, constructor: (ActorRef<T>) -> T) =
        Actor(ActorContext.current.get(),this, scheduler, constructor, name, nextActorId.getAndIncrement())

    override fun shutdown() = scheduler.shutdown()

    fun join() = scheduler.join()
}