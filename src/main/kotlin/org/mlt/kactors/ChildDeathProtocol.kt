package org.mlt.kactors

interface ChildDeathProtocol {
    fun childDied(ref: ActorRef<*>, e: Exception)
}