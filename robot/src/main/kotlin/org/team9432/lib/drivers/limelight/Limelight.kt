package org.team9432.lib.drivers.limelight

import edu.wpi.first.math.geometry.Pose3d
import org.littletonrobotics.junction.Logger
import org.team9432.lib.commandbased.KSubsystem

object Limelight: KSubsystem() {
    private val inputs = LimelightIOInputsAutoLogged()
    private val io = LimelightIONetworkTables()

    override fun constantPeriodic() {
        io.updateInputs(inputs)
        Logger.processInputs("Limelight", inputs)
    }

    fun getTagRelativeCameraPose(): Pose3d {
        return inputs.cameraPose
    }

    fun setLEDMode(ledMode: LimelightIONetworkTables.LEDMode) {
        io.setLEDMode(ledMode); println(ledMode)
    }
}