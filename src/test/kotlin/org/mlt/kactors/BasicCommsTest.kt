package org.mlt.kactors

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestActor(private val self: ActorRef<TestActor>) {
    lateinit var msg: String

    fun message(s: String) {
        msg = s
        die()
    }

    fun message2(s: String) {
        msg = s
    }

    fun die() {
        self.context().shutdown()
    }

    fun question(s: String) = s.length

    fun askLength(s: String) {
        val other = self.context().actorOf("ask") { TestActor(it) }
        other.ask({ question(s) }) {
            msg = "pituus=$it"
            self.context().shutdown()
        }
    }
}

class BasicCommsTest {
    @Test
    fun testBasicTell() {
        val system = ActorSystem()
        var actor: TestActor? = null
        val root = system.actorOf("root") { self -> TestActor(self).also { actor = it } }
        root.tell { message("foo") }
        system.join()
        assertNotNull(actor)
        assertEquals("foo", actor!!.msg)
    }

    @Test
    fun testDelayedTell() {
        val system = ActorSystem()
        var actor: TestActor? = null
        val root = system.actorOf("root") { self -> TestActor(self).also { actor = it } }
        val startTime = System.nanoTime()
        root.tellAfter(500) { message("foo") }
        root.tellAfter(200) { message2("bar") }
        system.join()
        val endTime = System.nanoTime()
        assertNotNull(actor)
        assertTrue((endTime-startTime)>500000)
        assertEquals("foo", actor!!.msg)
    }

    @Test
    fun testBasicAsk() {
        val system = ActorSystem()
        var actor: TestActor? = null
        val root = system.actorOf("root") { self -> TestActor(self).also { actor = it } }
        root.tell { askLength("foobar") }
        system.join()
        assertNotNull(actor)
        assertEquals("pituus=6", actor!!.msg)
    }

    @Test
    fun testFutureAsk() {
        val system = ActorSystem()
        val root = system.actorOf("root") { TestActor(it) }
        val future = root.ask { question("bar") }
        root.tell { die() }
        system.join()
        assertEquals(3, future.get())
    }
}