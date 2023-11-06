package org.team9432.lib.field

class Region(private vararg val regions: BaseRegion): BaseRegion {
    override fun contains(point: Point) = regions.any { it.contains(point) }
    override fun getPoints(): List<Point> {
        val points = mutableSetOf<Point>()
        regions.forEach { points.addAll(it.getPoints()) }
        return points.toList()
    }
}