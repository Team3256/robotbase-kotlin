package org.team9432.lib.util

import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation3d
import org.littletonrobotics.junction.Logger
import org.team9432.lib.field.Point

object LoggerUtil {
    fun recordPoints(key: String, points: List<Point>) {
        val x = points.map { Pose3d(it.x, it.y, 0.0, Rotation3d(0.0, Math.toRadians(-90.0), 0.0)) }.toTypedArray()
        Logger.recordOutput(key, *x)
    }
}
