package org.mlt.mandelbrot

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JPanel

fun Graphics2D.drawPoint(x: Int, y:Int, r:Int, g:Int, b:Int) {
    color = Color(r,g,b)
    drawLine(x,y,x,y)
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
