package org.team9432

import edu.wpi.first.net.PortForwarder
import edu.wpi.first.wpilibj.PowerDistribution
import edu.wpi.first.wpilibj.PowerDistribution.ModuleType
import org.littletonrobotics.junction.LogFileUtil
import org.littletonrobotics.junction.LoggedRobot
import org.littletonrobotics.junction.Logger
import org.littletonrobotics.junction.networktables.NT4Publisher
import org.littletonrobotics.junction.wpilog.WPILOGReader
import org.littletonrobotics.junction.wpilog.WPILOGWriter
import org.team9432.lib.commandbased.KCommandScheduler
import org.team9432.lib.drivers.limelight.Limelight
import org.team9432.robot.Controls


/**
 * The VM is configured to automatically run this object (which basically functions as a singleton class),
 * and to call the functions corresponding to each mode, as described in the TimedRobot documentation.
 * This is written as an object rather than a class since there should only ever be a single instance, and
 * it cannot take any constructor arguments. This makes it a natural fit to be an object in Kotlin.
 *
 * If you change the name of this object or its package after creating this project, you must also update
 * the `Main.kt` file in the project. (If you use the IDE's Rename or Move refactorings when renaming the
 * object or package, it will get changed everywhere.)
 */
object Robot: LoggedRobot() {
    val mode = Mode.REAL

    override fun robotInit() {
        Logger.recordMetadata("ProjectName", "Swerve") // Set a metadata value

        if (isReal() || mode == Mode.SIM) {
//            Logger.addDataReceiver(WPILOGWriter("/U")) // Log to a USB stick
            Logger.addDataReceiver(NT4Publisher()) // Publish data to NetworkTables
            PowerDistribution(1, ModuleType.kRev) // Enables power distribution logging
        } else if (mode == Mode.REPLAY) {
            setUseTiming(false) // Run as fast as possible
            val logPath = LogFileUtil.findReplayLog() // Pull the replay log from AdvantageScope (or prompt the user)
//            Logger.setReplaySource(WPILOGReader(logPath)) // Read replay log
//            Logger.addDataReceiver(WPILOGWriter(LogFileUtil.addPathSuffix(logPath, "_sim")))
        }

        Logger.start() // Start logging! No more data receivers, replay sources, or metadata values may be added.

        for (port in 5800..5807) {
            PortForwarder.add(port, "10.94.32.11", port)
        }

        Controls
//        Limelight
    }

    override fun robotPeriodic() {
        KCommandScheduler.run()
    }

    override fun autonomousInit() {}

    override fun autonomousPeriodic() {}

    override fun teleopInit() {}

    override fun teleopPeriodic() {}

    override fun disabledInit() {}

    override fun disabledPeriodic() {}

    override fun testInit() {}

    override fun testPeriodic() {}

    override fun simulationInit() {}

    override fun simulationPeriodic() {}


    enum class Mode {
        REAL, SIM, REPLAY
    }
}
