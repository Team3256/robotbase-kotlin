package org.team9432.lib.field

import java.awt.geom.Line2D

class Line(p1: Point, p2: Point): Line2D.Double(p1, p2), BaseRegion {
    override fun getPoints() = listOf(Point(x1, y1), Point(x2, y2))
    override fun contains(point: Point) = false
}