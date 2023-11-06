package org.team9432.lib.field

import java.awt.geom.Rectangle2D

class Rectangle(x: kotlin.Double, y: kotlin.Double, w: kotlin.Double, h: kotlin.Double): Rectangle2D.Double(x, y, w, h), BaseRegion {
    override fun getPoints(): List<Point> {
        val bottomLeft = Point(x + width, y)
        val bottomRight = Point(x + width, y + height)
        val upperRight = Point(x, y + height)
        val upperLeft = Point(x, y)
        return listOf(bottomLeft, bottomRight, upperRight, upperLeft)
    }

    override fun contains(point: Point): Boolean {
        return super.contains(point)
    }
}
