package org.mlt.kactors

import java.util.concurrent.Executor

interface ActorRef<T> : Executor {
    fun tell(msg: T.() -> Unit)
    fun <R> ask(msg: T.() -> R, callback: (R) -> Unit)
    suspend fun <R> askSuspend(msg: T.() -> R): R

    fun context(): ActorContext

    operator fun rem(msg: T.() -> Unit) = tell(msg)
    suspend operator fun <R> div(msg: T.() -> R): R = askSuspend(msg)
}
