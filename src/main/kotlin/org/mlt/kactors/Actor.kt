package org.mlt.kactors

class Actor<T>(
    private val actorContext: ActorContext,
    private val scheduler: Scheduler,
    private val actorFactory: (ActorRef<T>) -> T,
    name: String,
    private val actorId: Int,
    private val serializer:  (() -> Serializer<T>)?,
) : ActorRef<T>
{
    private var actor: T? = null

    override fun tell(msg: T.() -> Unit) {
        execute {
            if(actor==null) {
                actor = actorFactory(this)
            }
            serializer?.invoke()?.let {
                msg(it.real())
                println(it.serialize())
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
            serializer?.invoke()?.let {
                msg(it.real())
                println(it.serialize())
            }
            val r = msg(actor!!)
            caller.tell { callback(r) }
        }
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