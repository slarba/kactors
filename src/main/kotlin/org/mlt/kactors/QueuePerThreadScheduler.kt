package org.mlt.kactors

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class QueuePerThreadScheduler(nThreads: Int) : Scheduler {
    private val queues = Array(nThreads) { Queue() }
    private val timers = Executors.newSingleThreadScheduledExecutor()

    override fun schedule(actorId: Int, action: () -> Unit) =
        queues[actorId % queues.size].put(action)
    override fun scheduleAfterDelay(delay: Long, actorId: Int, action: () -> Unit): ScheduledFuture<*> {
        return timers.schedule({ schedule(actorId, action) }, delay, TimeUnit.MILLISECONDS)
    }

    fun shutdown() = queues.forEach { it.shutdown() }

    fun join() = queues.forEach { it.join() }
}
