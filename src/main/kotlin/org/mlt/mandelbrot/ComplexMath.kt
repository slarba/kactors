package org.mlt.mandelbrot

import kotlin.math.sqrt

data class Subdivision(val lowerLeft: Complex, val topRight: Complex, val cx: Int, val cy: Int) {
    @OptIn(ExperimentalStdlibApi::class)
    fun forEachPoint(xd: Int, yd: Int, fn: (Complex, Int, Int) -> Unit) {
        val diff = topRight - lowerLeft
        val dx = diff.re/xd
        val dy = diff.im/yd
        for(x in 0..< xd) {
            for(y in 0..< yd) {
                val c = Complex(lowerLeft.re + (dx * x), lowerLeft.im + (dy * y))
                fn(c, x, y)
            }
        }
    }
}

data class Complex(val re: Double = .0, val im: Double = .0) {
    operator fun plus(o: Complex) = Complex(re + o.re, im + o.im)
    operator fun minus(o: Complex) = Complex(re - o.re, im - o.im)

    operator fun times(o: Complex) = Complex(re * o.re - im * o.im, re * o.im + im * o.re)

    fun abs() = sqrt(re * re + im * im)

    @OptIn(ExperimentalStdlibApi::class)
    fun subdivisionsTo(o: Complex, xd: Int, yd: Int, width: Int, height: Int): List<Subdivision> {
        val diff = o-this
        val dre = diff.re/xd
        val dim = diff.im/yd
        val xw = width/xd
        val yh = height/yd
        val result = mutableListOf<Subdivision>()
        for(x in 0..< xd) {
            for (y in 0..< yd) {
                val sd = Subdivision(
                    this + Complex(re + (x * dre), im + (y * dim)),
                    this + Complex(re + ((x + 1) * dre), im + ((y + 1) * dim)),
                    (xw * x),
                    (yh * y)
                )
                result.add(sd)
            }
        }
        return result
    }
}