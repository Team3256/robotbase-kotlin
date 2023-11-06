package org.team9432.lib.field

interface BaseRegion {
    fun getPoints(): List<Point>
    operator fun contains(point: Point): Boolean
}
