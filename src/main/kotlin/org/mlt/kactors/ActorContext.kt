package org.mlt.kactors

interface ActorContext {
    fun <T> actorOf(name: String, constructor: (ActorRef<T>) -> T): ActorRef<T>
    fun shutdown()

    companion object {
        val current: ThreadLocal<Actor<*>> = ThreadLocal()
    }
}
