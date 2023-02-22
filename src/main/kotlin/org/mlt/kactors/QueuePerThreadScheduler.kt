package org.mlt.kactors

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class QueuePerThreadScheduler(
    nThreads: Int
) : Scheduler {
    private val queues = Array(nThreads) { Queue() }

    class Queue {
        private val queue = ConcurrentLinkedQueue<() -> Unit>()
        private val running = AtomicBoolean(true)
        private val handlerThread = Thread(this::handler)
        private val shutdown = AtomicBoolean(false)
        private val lock = ReentrantLock()
        private val condition = lock.newCondition()

        init {
            handlerThread.start()
        }

        private fun handler() {
            while(true) {
                val job = queue.poll()
                if(job==null) {
                    if(shutdown.get()) break
                    running.set(false)
                    lock.withLock {
                        condition.await()
                        running.set(true)
                    }
                    continue
                }
                job()
            }
        }

        fun put(job: () -> Unit) {
            queue.add(job)
            if(!running.get()) {
                lock.withLock { condition.signal() }
            }
        }

        fun join() {
            handlerThread.join()
        }

        fun shutdown() {
            shutdown.set(true)
            lock.withLock { condition.signal() }
        }
    }

    override fun schedule(actorId: Int, action: () -> Unit) {
        queues[actorId % queues.size].put(action)
    }

    fun shutdown() {
        queues.forEach { it.shutdown() }
    }

    fun join() {
        queues.forEach { it.join() }
    }
}
