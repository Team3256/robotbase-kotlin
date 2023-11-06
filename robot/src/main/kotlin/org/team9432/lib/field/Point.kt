package org.team9432.lib.field

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import java.awt.geom.Point2D

class Point(x: kotlin.Double, y: kotlin.Double): Point2D.Double(x, y), BaseRegion {
    override fun getPoints() = listOf(this)
    override fun contains(point: Point) = false
}

fun Point.toPose2d() = Pose2d(x, y, Rotation2d())
fun Pose2d.toPoint() = Point(x, y)