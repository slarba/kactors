package org.mlt.kactors

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

interface ActorRef<T> : Executor {
    fun tell(msg: T.() -> Unit)
    fun tellAfter(delay: Long, msg: T.() -> Unit)
    fun <R> ask(msg: T.() -> R, callback: (R) -> Unit)
    fun <R> ask(msg: T.() -> R): CompletableFuture<R>

    fun context(): ActorContext
    fun reportChildDeath(ref: ActorRef<*>, e: Exception)
    fun isAlive(): Boolean
}
