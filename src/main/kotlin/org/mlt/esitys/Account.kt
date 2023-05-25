package org.mlt.esitys

class Account {
    private var balance: Long = 2000000

    fun debit(amount: Long): Boolean {
        if(balance<amount) return false
        balance -= amount
        return true
    }

    fun printBalance() {
        println("balance = $balance")
    }
}

fun main() {
    val p = Account()

    val fn = {
        for(i in 1..1000001) {
            val success = p.debit(1)
            if(!success) {
                println("could not debit, no funds!")
                break
            }
        }
    }
    val thread1 = Thread(fn).also { it.start() }
    val thread2 = Thread(fn).also { it.start() }
    thread1.join()
    thread2.join()

    p.printBalance()
}