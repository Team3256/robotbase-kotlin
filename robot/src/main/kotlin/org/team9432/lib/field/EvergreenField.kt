package org.team9432.lib.field

import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation3d
import org.littletonrobotics.junction.Logger

@Suppress("MemberVisibilityCanBePrivate")
object EvergreenField {
    /** Y */
    const val FIELD_HEIGHT = 8.0137
    /** X */
    const val FIELD_WIDTH = 16.54175
    const val FIELD_MIDLINE = FIELD_WIDTH / 2

    fun Point.flip() = Point(FIELD_MIDLINE + (FIELD_MIDLINE - x), y)
    fun Rectangle.flip() = Rectangle(FIELD_MIDLINE + (FIELD_MIDLINE - width - x), y, width, height)

    fun recordPoints(key: String, points: List<Point>) {
        val x = points.map { Pose3d(it.x, it.y, 0.0, Rotation3d(0.0, Math.toRadians(-90.0), 0.0)) }.toTypedArray()
        Logger.recordOutput(key, *x)
    }
}