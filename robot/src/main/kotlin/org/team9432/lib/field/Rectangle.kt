package org.team9432.lib.field

import java.awt.geom.Rectangle2D

open class Rectangle(x: kotlin.Double, y: kotlin.Double, val w: kotlin.Double, val h: kotlin.Double): Rectangle2D.Double(x, y, w, h) {
    fun getPoints(): List<Point> {
        val bottomLeft = Point(x + width, y)
        val bottomRight = Point(x + width, y + height)
        val upperRight = Point(x, y + height)
        val upperLeft = Point(x, y)
        return listOf(bottomLeft, bottomRight, upperRight, upperLeft)
    }

    fun expand(distance: kotlin.Double) = Rectangle(x - distance, y - distance, w + distance + distance, h + distance + distance)
    val center: Point get() = Point(super.getCenterX(), super.getCenterY())
    fun getAxes() = listOf(Rectangle(centerX, y, 0.1, h), Rectangle(x, centerY, w, 0.1))
}
