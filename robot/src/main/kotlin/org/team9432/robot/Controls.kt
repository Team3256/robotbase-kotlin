package org.team9432.robot


import org.team9432.lib.commandbased.commands.InstantCommand
import org.team9432.lib.commandbased.input.KXboxController
import org.team9432.robot.subsystems.drivetrain.Drivetrain
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.withSign

object Controls {
    private val controller = testDriver

    init {
        Drivetrain.defaultCommand =
            Drivetrain.fieldOrientedDriveCommand({ controller.leftY }, { controller.leftX }, { controller.rightX }, maxSpeedDegreesPerSecond = 360.0, maxSpeedMetersPerSecond = 4.0)

        controller.a.onTrue(InstantCommand { Drivetrain.resetGyro() })
    }
}

private val testDriver = KXboxController(0, { input ->
    input.applyDeadband(0.15).applyExponential(3.0)
})


private fun Double.applyDeadband(value: Double) = if (abs(this) > value) this else 0.0
private fun Double.applyExponential(value: Double) = this.pow(value).withSign(this)