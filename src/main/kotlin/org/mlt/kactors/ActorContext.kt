package org.mlt.kactors

interface ActorContext {
    fun <T> actorOf(name: String, serializer: (() -> Serializer<T>)? = null, constructor: (ActorRef<T>) -> T): ActorRef<T>
    fun shutdown()

    companion object {
        val current: ThreadLocal<Actor<*>> = ThreadLocal()
    }
}
