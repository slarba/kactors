package org.mlt.kactors

import java.util.concurrent.atomic.AtomicInteger

class ActorSystem(nThreads: Int = 32) : ActorContext {
    private val nextActorId = AtomicInteger()
    private val scheduler = QueuePerThreadScheduler(nThreads)

    override fun <T> actorOf(name: String, constructor: (ActorRef<T>) -> T) =
        Actor(ActorContext.current.get(),this, scheduler, constructor, name, nextActorId.getAndIncrement())

    override fun <T> actorOf(
        name: String,
        recoveryStrategy: RecoveryStrategy,
        constructor: (ActorRef<T>) -> T
    ): ActorRef<T> =
        Actor(ActorContext.current.get(),this, scheduler, constructor, name, nextActorId.getAndIncrement(), recoveryStrategy)

    override fun shutdown() = scheduler.shutdown()

    fun join() = scheduler.join()
}