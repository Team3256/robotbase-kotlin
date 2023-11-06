package org.team9432.lib.drivers.limelight

import edu.wpi.first.math.geometry.Pose3d
import org.team9432.lib.annotation.Logged


interface LimelightIO {
    @Logged
    open class LimelightIOInputs {
        var cameraPose = Pose3d()
        var tagID = 0.0
        var xCorners: DoubleArray = DoubleArray(0)
        var yCorners: DoubleArray = DoubleArray(0)
    }

    fun updateInputs(inputs: LimelightIOInputs)

    fun setLEDMode(ledMode: LimelightIONetworkTables.LEDMode) {}
}