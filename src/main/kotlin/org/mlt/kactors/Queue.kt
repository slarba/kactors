package org.mlt.kactors

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

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
        while(!shutdown.get()) {
            val msg = queue.poll()
            if(msg!=null) {
                msg()
                continue
            }
            running.set(false)
            lock.withLock {
                condition.await()
                running.set(true)
            }
        }
    }

    fun put(job: () -> Unit) {
        queue.add(job)
        signal()
    }

    fun join() = handlerThread.join()

    fun shutdown() {
        shutdown.set(true)
        signal()
    }

    private fun signal() {
        if(!running.get()) {
            lock.withLock { condition.signal() }
        }
    }
}