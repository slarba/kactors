package org.mlt.kactors

import kotlinx.coroutines.CompletableDeferred

class Actor<T>(
    private val actorContext: ActorContext,
    private val scheduler: Scheduler,
    private val actorFactory: (ActorRef<T>) -> T,
    name: String,
    private val actorId: Int
) : ActorRef<T>
{
    private var actor: T? = null

    override fun tell(msg: T.() -> Unit) {
        execute {
            if(actor==null) {
                actor = actorFactory(this)
            }
            msg(actor!!)
        }
    }

    override fun <R> ask(msg: T.() -> R, callback: (R) -> Unit) {
        val caller = ActorContext.current.get()
        execute {
            if(actor==null) {
                actor = actorFactory(this)
            }
            val r = msg(actor!!)
            caller.tell { callback(r) }
        }
    }

    override suspend fun <R> askSuspend(msg: T.() -> R): R {
        val caller = ActorContext.current.get()
        val result = CompletableDeferred<R>()
        execute {
            if(actor==null) {
                actor = actorFactory(this)
            }
            val r = msg(actor!!)
            caller.tell { result.complete(r) }
        }
        return result.await()
    }

    override fun context() = actorContext

    override fun execute(job: Runnable) {
        scheduler.schedule(actorId) {
            ActorContext.current.set(this)
            try {
                job.run()
            } finally {
                ActorContext.current.set(null)
            }
        }
    }
}