package org.mlt.esitys

import org.mlt.kactors.ActorRef
import org.mlt.kactors.ActorSystem

class ActorAccount(private var balance: Long) {
    fun debit(amount: Long): Boolean {
        if(balance<amount) return false
        balance -= amount
        return true
    }

    fun printBalance() {
        println("balance = $balance")
    }
}

class Debitor(private val name: String, private val main: ActorRef<MainActor>, private val account: ActorRef<ActorAccount>) {
    private val ITERATIONS = 1000001

    fun start() {
        for(i in 1..ITERATIONS) {
            account.ask({ debit(1) }) { success ->
                if(!success) {
                    println("$name could not debit!")
                }
                if(i==ITERATIONS) main.tell { finished() }
            }
        }
    }
}

class MainActor(private val self: ActorRef<MainActor>) {
    private val account = self.context().actorOf("account") { ActorAccount(2000000) }

    fun start() {
        val debitor1 = self.context().actorOf("debitor1") { Debitor("debitor1", self, account) }
        val debitor2 = self.context().actorOf("debitor2") { Debitor("debitor2", self, account) }

        debitor1.tell { start() }
        debitor2.tell { start() }
    }

    private var nFinished = 0

    fun finished() {
        if(++nFinished<2) return
        account.ask({ printBalance() }) {
            self.context().shutdown()
        }
    }
}

fun main() {
    val actorSystem = ActorSystem(4)
    val mainActor = actorSystem.actorOf("main") { self -> MainActor(self) }
    mainActor.tell { start() }
}
