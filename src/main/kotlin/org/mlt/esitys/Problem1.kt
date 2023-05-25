package org.mlt.esitys

class Problem1 {
    private var counter: Long = 0

    fun increment() = counter++

    fun printCounter() {
        println("counter = $counter")
    }
}

fun main() {
    val p = Problem1()

    val fn = { for(i in 1..1000000) p.increment() }
    val thread1 = Thread(fn).also { it.start() }
    val thread2 = Thread(fn).also { it.start() }
    thread1.join()
    thread2.join()

    p.printCounter()
}