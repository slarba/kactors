package org.mlt.mandelbrot

import org.mlt.kactors.ActorRef
import org.mlt.kactors.ActorSystem
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel

class SubdivisionComputer {
    fun compute(sd: Subdivision, xp: Int, yp: Int, result: ActorRef<MandelbrotComputer>) {
        val image = BufferedImage(xp, yp, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        sd.forEachPoint(xp,yp) { c, x, y ->
            val col = mandelbrot(c, 500)
            drawPoint(graphics, x, y, col%200, col % 150, col % 10)
        }
        result.tell { subdivisionReady(image, sd.cx, sd.cy) }
    }

    private fun mandelbrot(c: Complex, iterations: Int): Int {
        var z = Complex(.0,.0)
        for(i in 0..iterations) {
            z = z*z + c
            if(z.abs()>2.0) return i
        }
        return iterations
    }

    private fun drawPoint(graphics: Graphics2D, x: Int, y: Int, r: Int, g: Int, b: Int) {
        graphics.color = Color(r,g,b)
        graphics.drawLine(x,y,x,y)
    }
}

class MandelbrotComputer(private val self: ActorRef<MandelbrotComputer>, private val viewer: Viewer) {
    private lateinit var subdivisions: Iterator<Subdivision>;

    fun start(from: Complex, to: Complex, xd: Int, yd: Int) {
        subdivisions = from.subdivisionsTo(to, xd, yd, viewer.width(), viewer.height()).iterator()
        self.tell { next(xd,yd) }
    }

    fun next(xd: Int, yd: Int) {
        if(subdivisions.hasNext()) {
            val sd = subdivisions.next()
            val actor = self.context().actorOf("subdivision") { SubdivisionComputer() }
            actor.tell { compute(sd, viewer.width()/xd, viewer.height()/yd, self) }
            self.tell { next(xd, yd) }
        }
    }

    fun subdivisionReady(img: BufferedImage, cx: Int, cy: Int) = viewer.drawSubdivision(img, cx, cy)
}

class Observable<T>(private val self: ActorRef<Observable<T>>) {
    private lateinit var items: Iterator<T>
    private lateinit var actOn: (ActorRef<Observable<T>>,T) -> Unit

    fun stream(items: Iterable<T>, action: (ActorRef<Observable<T>>,T) -> Unit) {
        this.items = items.iterator()
        this.actOn = action
        self.tell { next() }
    }

    private fun next() {
        if(items.hasNext()) {
            val item = items.next()
            actOn(self,item)
            self.tell { next() }
        }
    }

    companion object {
        fun <T> create(system: ActorSystem, items: Iterable<T>, action: (ActorRef<Observable<T>>,T) -> Unit) {
            val actor = system.actorOf("observable") { Observable<T>(it) }
            actor.tell { stream(items, action) }
        }
    }
}

class Viewer(w: Int, h: Int) : JFrame() {
    private val image = BufferedImage(w,h, BufferedImage.TYPE_INT_RGB)
    private val graphics = image.createGraphics()
    private val panel = object : JPanel() {
        override fun paint(g: Graphics?) {
            super.paint(g)
            g!!.drawImage(image,0,0,null)
        }
    }

    init {
        contentPane.add(panel)
        setSize(w,h)
        isVisible = true
    }

    fun width() = image.width
    fun height() = image.height

    fun drawSubdivision(img: BufferedImage, cx: Int, cy: Int) {
        graphics.drawImage(img, cx, cy, null)
        panel.repaint()
    }
}

fun main() {
    val system = ActorSystem()

    val viewer = Viewer(1500,1500)
    val mandelbrot = system.actorOf("mandelbrot") { MandelbrotComputer(it, viewer) }
    mandelbrot.tell { start(Complex(-.6, -.6), Complex(1.0,1.0), 50, 50) }
}
