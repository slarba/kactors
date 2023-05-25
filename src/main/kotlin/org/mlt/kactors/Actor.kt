package org.mlt.kactors

import java.util.concurrent.CompletableFuture

class Actor<T>(
    private val parent: ActorRef<*>?,
    private val actorContext: ActorContext,
    private val scheduler: Scheduler,
    private val actorFactory: (ActorRef<T>) -> T,
    private val name: String,
    private val actorId: Int,
) : ActorRef<T>
{
    private var actor: T? = null
    private var alive: Boolean = true

    override fun equals(other: Any?): Boolean {
        if(other !is Actor<*>) return false
        return (other.actorId == actorId)
    }

    override fun hashCode(): Int = actorId.hashCode()

    override fun toString(): String = "Actor($name:$actorId)"

    override fun tell(msg: T.() -> Unit) {
        execute {
            if(actor==null) {
                actor = actorFactory(this)
            }
            msg(actor!!)
        }
    }

    override fun tellAfter(delay: Long, msg: T.() -> Unit) {
        executeAfter(delay) {
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
            caller?.tell { callback(r) }
        }
    }

    override fun <R> ask(msg: T.() -> R): CompletableFuture<R> {
        val future = CompletableFuture<R>()
        execute {
            if(actor==null) {
                actor = actorFactory(this)
            }
            val r = msg(actor!!)
            future.complete(r)
        }
        return future
    }

    override fun context() = actorContext
    override fun reportChildDeath(ref: ActorRef<*>, e: Exception) {
        if(actor!=null && actor is ChildDeathProtocol) {
            tell {
                (this as ChildDeathProtocol).childDied(ref, e)
            }
        }
    }

    override fun isAlive(): Boolean {
        return alive
    }

    override fun execute(job: Runnable) {
        scheduler.schedule(this, actorId) {
            ActorContext.current.set(this)
            try {
                job.run()
            } catch(e: Exception) {
                alive = false
                parent?.reportChildDeath(this, e)
            } finally {
                ActorContext.current.set(null)
            }
        }
    }

    fun executeAfter(delay: Long, job: Runnable) {
        scheduler.scheduleAfterDelay(delay, this, actorId) {
            ActorContext.current.set(this)
            try {
                job.run()
            } catch(e: Exception) {
                alive = false
                parent?.reportChildDeath(this, e)
            } finally {
                ActorContext.current.set(null)
            }
        }
    }

}