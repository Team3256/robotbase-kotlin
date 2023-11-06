package org.team9432.swerve.subsystems.drivetrain

import edu.wpi.first.math.kinematics.SwerveModuleState
import org.littletonrobotics.junction.AutoLog
import org.team9432.lib.annotation.Logged

interface ModuleIO {
    @Logged
    open class ModuleIOInputs {
        var positionMeters = 0.0
        var speedMetersPerSecond = 0.0
        var angle = 0.0
    }

    fun updateInputs(inputs: ModuleIOInputs)

    fun setBrakeMode(enabled: Boolean) {}
    fun updateIntegratedEncoder() {}
    fun setState(state: SwerveModuleState) {}

    var disabled: Boolean

    val position: Position

    enum class Position {
        FL, FR, BL, BR
    }
}