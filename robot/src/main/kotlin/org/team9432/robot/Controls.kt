package org.team9432.robot


import org.team9432.lib.commandbased.commands.InstantCommand
import org.team9432.lib.commandbased.input.KXboxController
import org.team9432.lib.drivers.limelight.Limelight
import org.team9432.lib.drivers.limelight.LimelightIONetworkTables
import org.team9432.robot.subsystems.drivetrain.Drivetrain

object Controls {
    private val controller = KXboxController(0)

    init {
        Drivetrain.defaultCommand = Drivetrain.fieldOrientedDriveCommand({ -controller.leftY }, { -controller.leftX }, { -controller.rightX })

        controller.a.onTrue(InstantCommand { Limelight.setLEDMode(LimelightIONetworkTables.LEDMode.FORCE_ON) })
        controller.b.onTrue(InstantCommand { Limelight.setLEDMode(LimelightIONetworkTables.LEDMode.FORCE_OFF) })
    }
}
