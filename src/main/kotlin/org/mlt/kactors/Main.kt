package org.mlt.kactors

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TestActor(private val self: ActorRef<TestActor>) {
    fun question(s: String): Int {
        Thread.sleep(5000)
        return s.length
    }
}

class RootActor(private val self: ActorRef<RootActor>) {
    fun start() {
        println("Started")
        val test = self.context().actorOf("test") { TestActor(it) }
//        runBlocking(test.asCoroutineDispatcher()) {
//            launch { println(test / { question("yksikaksi") }) }
//            println("Launched")
//        }
        test.ask({ question("foobar") }) {
            println("Pituus = $it")
        }
        println("Returned")
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem()

    val root = system.actorOf("root") { RootActor(it) }

    root % RootActor::start

    system.join()
}
