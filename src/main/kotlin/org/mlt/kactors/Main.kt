package org.mlt.kactors

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
        test.ask({ question("foobar") }) {
            println("Pituus = $it")
        }
        println("Returned")
    }

    fun other() {
        println("In other!")
    }
}

fun main(args: Array<String>) {
    val system = ActorSystem()

    val root = system.actorOf("root") { RootActor(it) }

    root.tell { start() }
    root.tell { other() }

    system.join()
}
