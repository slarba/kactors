package org.mlt.kactors

import java.util.concurrent.ScheduledFuture

interface Scheduler {
    fun schedule(ref: ActorRef<*>, actorId: Int, action: () -> Unit)
    fun scheduleAfterDelay(delay: Long, ref: ActorRef<*>, actorId: Int, action: () -> Unit): ScheduledFuture<*>
}