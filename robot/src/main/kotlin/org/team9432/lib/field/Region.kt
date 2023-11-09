package org.team9432.lib.field

interface Region {
    fun getPoints(): List<Point>
    fun contains(point: Point): Boolean
    fun intersects(line: Line): Boolean
    fun expand(distance: Double): Region
}