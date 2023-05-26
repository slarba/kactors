package org.mlt.mandelbrot

import org.mlt.kactors.ActorRef
import org.mlt.kactors.ActorSystem
import java.awt.image.BufferedImage

const val ITERATIONS = 500

class SubdivisionComputer {
    fun compute(sd: Subdivision, xp: Int, yp: Int, result: ActorRef<MandelbrotComputer>) {
        val image = BufferedImage(xp, yp, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()

        sd.forEachPoint(xp,yp) { c, x, y ->
            val col = mandelbrot(c, ITERATIONS)
            graphics.drawPoint(x, y, col%200, col % 150, col % 10)
        }
        result.tell { subdivisionReady(image, sd.cx, sd.cy) }
    }

    private fun mandelbrot(c: Complex, iterations: Int): Int {
        var z = Complex()
        for(i in 0..iterations) {
            z = z*z + c
            if(z.abs()>2.0) return i
        }
        return iterations
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
            actor.tell {
                compute(sd, viewer.width()/xd, viewer.height()/yd, self)
            }
            self.tell { next(xd, yd) }
        }
    }

    fun subdivisionReady(img: BufferedImage, cx: Int, cy: Int) = viewer.drawSubdivision(img, cx, cy)
}


fun main() {
    val system = ActorSystem(16)

    val viewer = Viewer(1500,1500)
    val mandelbrot = system.actorOf("mandelbrot") { MandelbrotComputer(it, viewer) }
    mandelbrot.tell { start(Complex(-.6, -.6), Complex(1.0,1.0), 50, 50) }
}
