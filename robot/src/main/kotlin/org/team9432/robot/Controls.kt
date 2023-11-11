package org.team9432.robot


import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import org.team9432.lib.commandbased.commands.InstantCommand
import org.team9432.lib.commandbased.input.KXboxController
import org.team9432.lib.drivers.limelight.Limelight
import org.team9432.lib.drivers.limelight.LimelightIONetworkTables
import org.team9432.robot.subsystems.drivetrain.Drivetrain

object Controls {
    private val controller = KXboxController(0)

    init {
        Drivetrain.defaultCommand = Drivetrain.fieldOrientedDriveCommand({ -controller.leftY }, { -controller.leftX }, { -controller.rightX })

        controller.a.onTrue(Drivetrain.driveToPositionCommand(Pose2d(4.0, 5.0, Rotation2d.fromDegrees(180.0))))
        controller.b.onTrue(Drivetrain.driveToPositionCommand(Pose2d(7.0, 3.0, Rotation2d.fromDegrees(0.0))))
    }
}
