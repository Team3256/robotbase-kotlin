package org.team9432.lib.field

class RectangleGroup(private vararg val rectangles: Rectangle): Region {
    override fun contains(point: Point) = rectangles.any { it.contains(point) }
    override fun getPoints(): List<Point> {
        val points = mutableSetOf<Point>()
        rectangles.forEach { points.addAll(it.getPoints()) }
        return points.toList()
    }

    override fun intersects(line: Line) = rectangles.any { it.intersectsLine(line) }
}