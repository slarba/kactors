package org.mlt.kactors

class QueuePerThreadScheduler(nThreads: Int) : Scheduler {
    private val queues = Array(nThreads) { Queue() }

    override fun schedule(actorId: Int, action: () -> Unit) =
        queues[actorId % queues.size].put(action)

    fun shutdown() = queues.forEach { it.shutdown() }

    fun join() = queues.forEach { it.join() }
}
