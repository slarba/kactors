package org.mlt.kactors

import java.util.concurrent.Executor

interface ActorRef<T> : Executor {
    fun tell(msg: T.() -> Unit)
    fun <R> ask(msg: T.() -> R, callback: (R) -> Unit)

    fun context(): ActorContext
}
