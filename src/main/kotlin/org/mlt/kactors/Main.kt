package org.mlt.kactors

interface TestMessages {
    fun question(s: String): Int
}

interface RootMessages {
    fun start()
    fun other(i: Int)
}

class TestActor : TestMessages {
    override fun question(s: String): Int {
        Thread.sleep(5000)
        return s.length
    }
}

class RootActor(private val self: ActorRef<RootMessages>) : RootMessages {
    override fun start() {
        println("Started")
        val test = self.context().actorOf("test") { TestActor() }
        test.ask({ question("foobar") }) {
            println("Pituus = $it")
        }
        println("Returned")
    }

    override fun other(i: Int) {
        println("In other! $i")
    }
}

fun main() {
    val system = ActorSystem()

    val root = system.actorOf("root") { RootActor(it) }

    root.tell { start() }
    root.tell { other(42) }

    system.join()
}
