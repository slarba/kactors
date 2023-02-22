package org.mlt.kactors

interface Scheduler {
    fun schedule(actorId: Int, action: () -> Unit)
}