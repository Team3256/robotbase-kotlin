package org.team9432.lib.field

import edu.wpi.first.math.geometry.Pose3d
import edu.wpi.first.math.geometry.Rotation3d
import edu.wpi.first.math.util.Units
import edu.wpi.first.wpilibj.DriverStation.Alliance
import org.littletonrobotics.junction.Logger
import java.util.*


@Suppress("MemberVisibilityCanBePrivate")
object ChargedUp2023 {
    const val FIELD_HEIGHT = 8.0137
    const val FIELD_WIDTH = 16.54175
    const val FIELD_MIDLINE = FIELD_WIDTH / 2

    val redLoadingZone: Region = Region(Rectangle(0.0, 6.75, 6.71, 1.265), Rectangle(0.0, 6.75 - 1.24, 3.36, 1.24))
    val blueLoadingZone: Region = Region(flip(Rectangle(0.0, 6.75, 6.71, 1.265)), flip(Rectangle(0.0, 6.75 - 1.24, 3.36, 1.24)))
    val blueChargeStation: Rectangle = Rectangle(2.9, FIELD_HEIGHT - 6.4955, 1.95, 2.47)
    val redChargeStation: Rectangle = flip(blueChargeStation)
    val blueCommunity: Region = Region(Rectangle(1.37, FIELD_HEIGHT - 3.98, 1.985, 1.475), Rectangle(1.37, 0.0, 3.54, 4.0337))
    val redCommunity: Region = Region(flip(Rectangle(1.37, FIELD_HEIGHT - 3.98, 1.985, 1.475)), flip(Rectangle(1.37, 0.0, 3.54, 4.0337)))

    val stagingMarkers = listOf(
        Point(7.0775, FIELD_HEIGHT - 7.085),
        Point(7.0775, FIELD_HEIGHT - 5.862),
        Point(7.0775, FIELD_HEIGHT - 4.6396),
        Point(7.0775, FIELD_HEIGHT - 3.4219),
        flip(Point(7.0775, FIELD_HEIGHT - 7.085)),
        flip(Point(7.0775, FIELD_HEIGHT - 5.862)),
        flip(Point(7.0775, FIELD_HEIGHT - 4.6396)),
        flip(Point(7.0775, FIELD_HEIGHT - 3.4219))
    )

    fun flip(input: Point): Point {
        return Point(FIELD_MIDLINE + (FIELD_MIDLINE - input.x), input.y)
    }

    fun flip(input: Rectangle): Rectangle {
        return Rectangle(FIELD_MIDLINE + (FIELD_MIDLINE - input.width - input.x), input.y, input.width, input.height)
    }

    val blueLeft = Grid(Point(0.3657, FIELD_HEIGHT - 7.5033), Alliance.Blue)
    val blueCoOp = Grid(Point(0.3657, FIELD_HEIGHT - 5.8233), Alliance.Blue)
    val blueRight = Grid(Point(0.3657, FIELD_HEIGHT - 4.1503), Alliance.Blue)
    val redLeft = Grid(flip(Point(0.3657, FIELD_HEIGHT - 7.5033)), Alliance.Red)
    val redCoOp = Grid(flip(Point(0.3657, FIELD_HEIGHT - 5.8233)), Alliance.Red)
    val redRight = Grid(flip(Point(0.3657, FIELD_HEIGHT - 4.1503)), Alliance.Red)
    val redGrids = listOf(redLeft, redCoOp, redRight)
    val blueGrids = listOf(blueLeft, blueCoOp, blueRight)

    fun displayPoints(key: String, points: List<Point>) {
        val x = points.map { Pose3d(it.x, it.y, 0.0, Rotation3d(0.0, Math.toRadians(-90.0), 0.0)) }.toTypedArray()
        Logger.recordOutput(key, *x)
    }

    fun displayAll() {
        displayPoints("Field/Red Loading Zone", redLoadingZone.getPoints())
        displayPoints("Field/Blue Loading Zone", blueLoadingZone.getPoints())
        displayPoints("Field/Blue Charge Station", blueChargeStation.getPoints())
        displayPoints("Field/Red Charge Station", redChargeStation.getPoints())
        displayPoints("Field/Blue Community", blueCommunity.getPoints())
        displayPoints("Field/Red Community", redCommunity.getPoints())
        displayPoints("Field/StagingMarkers", stagingMarkers)

        val nodePoses = mutableListOf<Point>()
        val allGrids = mutableListOf<Grid>()
        allGrids.addAll(redGrids)
        allGrids.addAll(blueGrids)
        for (grid in allGrids) {
            for (row in grid.nodePoints) {
                for (node in row) {
                    nodePoses.add(node)
                }
            }
        }
        displayPoints("Field/Nodes", nodePoses)

        Logger.recordOutput("Field/AprilTags", *aprilTags.values.toTypedArray())
    }

    val aprilTags = mapOf(
        1 to Pose3d(Units.inchesToMeters(610.77), Units.inchesToMeters(42.19), Units.inchesToMeters(18.22), Rotation3d(0.0, 0.0, Math.PI)),
        2 to Pose3d(Units.inchesToMeters(610.77), Units.inchesToMeters(108.19), Units.inchesToMeters(18.22), Rotation3d(0.0, 0.0, Math.PI)),
        3 to Pose3d(Units.inchesToMeters(610.77), Units.inchesToMeters(174.19), Units.inchesToMeters(18.22), Rotation3d(0.0, 0.0, Math.PI)),
        4 to Pose3d(Units.inchesToMeters(636.96), Units.inchesToMeters(265.74), Units.inchesToMeters(27.38), Rotation3d(0.0, 0.0, Math.PI)),
        5 to Pose3d(Units.inchesToMeters(14.25), Units.inchesToMeters(265.74), Units.inchesToMeters(27.38), Rotation3d()),
        6 to Pose3d(Units.inchesToMeters(40.45), Units.inchesToMeters(174.19), Units.inchesToMeters(18.22), Rotation3d()),
        7 to Pose3d(Units.inchesToMeters(40.45), Units.inchesToMeters(108.19), Units.inchesToMeters(18.22), Rotation3d()),
        8 to Pose3d(Units.inchesToMeters(40.45), Units.inchesToMeters(42.19), Units.inchesToMeters(18.22), Rotation3d())
    )

    class Grid(topLeftConeNode: Point, alliance: Alliance) {
        // {topConeNodeL, topCubeNode, topConeNodeR},
        // {midConeNodeL, midCubeNode, midConeNodeR},
        // {hybridNodeL, hybridNodeM, hybridNodeR}
        private val allNodes: Array<Array<Point>> = Array<Array<Point>>(3) { Array<Point>(3) { Point(0.0, 0.0) } }

        init {
            val nodeSpacingHorizontal = 0.5576
            val nodeSpacingVertical = 0.4328
            for (v in allNodes.indices) {
                for (h in allNodes[0].indices) {
                    val i = if (alliance == Alliance.Blue) h else 2 - h
                    allNodes[v][i] = Point(topLeftConeNode.x + nodeSpacingVertical * if (alliance == Alliance.Blue) v else -v, topLeftConeNode.y + nodeSpacingHorizontal * h)
                }
            }
        }

        val nodePoints: Array<Array<Point>> get() = Arrays.copyOf(allNodes, 3)
    }
}