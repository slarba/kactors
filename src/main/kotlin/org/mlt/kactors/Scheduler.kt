package org.mlt.kactors

import java.util.concurrent.ScheduledFuture

interface Scheduler {
    fun schedule(actorId: Int, action: () -> Unit)
    fun scheduleAfterDelay(delay: Long, actorId: Int, action: () -> Unit): ScheduledFuture<*>
}