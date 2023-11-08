package org.team9432.lib.trajectory

import edu.wpi.first.math.geometry.Pose2d
import edu.wpi.first.math.geometry.Rotation2d
import org.team9432.lib.field.BaseRegion
import org.team9432.lib.field.EvergreenField
import org.team9432.lib.field.toPoint
import java.time.Instant
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * A* implementation from https://rosettacode.org/wiki/A*_search_algorithm#Kotlin
 *
 * The "resolution" of the grid can be defined with the precision parameter
 * Higher values will take longer, but the path will be defined in more/smaller steps and is therefore more precise
 * Default value is 4
 */
class AStar(precision: Double = 4.0, private vararg val obstacles: BaseRegion, displayGrid: Boolean = false) {
    private val gridWidth = (EvergreenField.FIELD_WIDTH * precision).roundToInt()
    private val gridHeight = (EvergreenField.FIELD_HEIGHT * precision).roundToInt()

    init {
        println("X size: $gridWidth, Y size: $gridHeight")

        if (displayGrid) {
            val mutableGrid = mutableListOf<Coordinate>()
            for (xCoordinate in 0..gridWidth) {
                for (yCoordinate in 0..gridHeight) {
                    mutableGrid.add(Coordinate(xCoordinate, yCoordinate))
                }
            }
            EvergreenField.recordPoints("Planner/Tiles", mutableGrid.map { coordinateToField(it).toPoint() })
        }
    }

    /** This function takes Pose2d positions and converts them into a grid system that is usable by A* */
    fun findPath(initialPose: Pose2d, finalPose: Pose2d, printTime: Boolean = false): MutableList<Pose2d>? {
        val start = if (printTime) Instant.now() else null
        val initialCoordinate = fieldToCoordinate(initialPose)
        val finalCoordinate = fieldToCoordinate(finalPose)

        val (path, cost) = searchAStar(initialCoordinate, finalCoordinate) ?: return null

        val waypoints = path.map { coordinateToField(it) }.toMutableList()
        waypoints.add(finalPose)
        waypoints.add(0, initialPose)

        if (printTime) println("Path generation time: ${Instant.now().toEpochMilli() - start!!.toEpochMilli()}ms")

//        println("Start: (x: ${initialCoordinate.x}, y: ${initialCoordinate.y}) End: (x: ${finalCoordinate.x}, y: ${finalCoordinate.y}) Cost: $cost Path: $path")
        return waypoints
    }

    /**
     * Implementation of the A* Search Algorithm to find the optimum path between 2 points on a grid.
     *
     * The Grid contains the details of the barriers and methods which supply the neighboring vertices and the cost of movement between 2 cells.
     */
    private fun searchAStar(start: Coordinate, finish: Coordinate): Pair<List<Coordinate>, Double>? {
        /** Use the cameFrom values to backtrack to the start position to generate the path */
        fun generatePath(currentPos: Coordinate, cameFrom: Map<Coordinate, Coordinate>): List<Coordinate> {
            val path = mutableListOf(currentPos)
            var current = currentPos
            while (cameFrom.containsKey(current)) {
                current = cameFrom.getValue(current)
                path.add(0, current)
            }
            return path.toList()
        }

        val openVertices = mutableSetOf(start)
        val closedVertices = mutableSetOf<Coordinate>()
        val costFromStart = mutableMapOf(start to 0.0)
        val estimatedTotalCost = mutableMapOf(start to heuristicDistance(start, finish))

        val cameFrom = mutableMapOf<Coordinate, Coordinate>() // Used to generate path by back tracking

        while (openVertices.size > 0) {
            val currentPos = openVertices.minBy { estimatedTotalCost.getValue(it) }

            // Check if we have reached the finish
            if (currentPos == finish) {
                // Backtrack to generate the most efficient path
                val path = generatePath(currentPos, cameFrom)
                return Pair(path, estimatedTotalCost.getValue(finish)) // First route to finish will be optimum route
            }

            // Mark the current vertex as closed
            openVertices.remove(currentPos)
            closedVertices.add(currentPos)

            getNeighbors(currentPos).filterNot { closedVertices.contains(it) }  // Exclude previously visited vertices
                .forEach { neighbor ->
                    val score = costFromStart.getValue(currentPos) + getMoveCost(currentPos, neighbor)
                    if (score < costFromStart.getOrDefault(neighbor, Double.MAX_VALUE)) {
                        if (!openVertices.contains(neighbor)) {
                            openVertices.add(neighbor)
                        }
                        cameFrom[neighbor] = currentPos
                        costFromStart[neighbor] = score
                        estimatedTotalCost[neighbor] = score + heuristicDistance(neighbor, finish)
                    }
                }
        }

        System.err.println("No Path from Start $start to Finish $finish!")
        return null
    }

    private val widthRange = 0 until gridWidth
    private val heightRange = 0 until gridHeight
    private fun inGrid(it: Coordinate) = (it.x in widthRange) && (it.y in heightRange)

    private val validMoves = mapOf( // translation to cost, diagonal moves are slightly more expensive
        Coordinate(1, 0) to 1.0,
        Coordinate(-1, 0) to 1.0,
        Coordinate(0, 1) to 1.0,
        Coordinate(0, -1) to 1.0,
        Coordinate(1, 1) to 1.5,
        Coordinate(-1, 1) to 1.5,
        Coordinate(1, -1) to 1.5,
        Coordinate(-1, -1) to 1.5
    )

    private fun getNeighbors(coordinate: Coordinate) = validMoves.keys.map { Coordinate(coordinate.x + it.x, coordinate.y + it.y) }.filter { inGrid(it) }

    private fun heuristicDistance(start: Coordinate, finish: Coordinate): Double {
        val dx = abs(start.x - finish.x)
        val dy = abs(start.y - finish.y)
        return (dx + dy) + (-2) * minOf(dx, dy).toDouble()
    }

    private fun getMoveCost(from: Coordinate, to: Coordinate): Double {
        if (obstacles.any { it.contains(coordinateToField(to).toPoint()) }) return Double.MAX_VALUE // Passing through walls is bad
        val movement = Coordinate(from.x - to.x, from.y - to.y)
        return validMoves[movement]!!
    }

    /** Converts a pose from the field coordinate system to the one used by A* */
    private fun fieldToCoordinate(pose: Pose2d): Coordinate {
        val newX = pose.x / (EvergreenField.FIELD_WIDTH / gridWidth)
        val newY = pose.y / (EvergreenField.FIELD_HEIGHT / gridHeight)
        return Coordinate(newX.roundToInt(), newY.roundToInt())
    }

    /** Converts a pose from the A* coordinate system to the field */
    private fun coordinateToField(coordinate: Coordinate): Pose2d {
        val newX = coordinate.x * (EvergreenField.FIELD_WIDTH / gridWidth)
        val newY = coordinate.y * (EvergreenField.FIELD_HEIGHT / gridHeight)
        return Pose2d(newX, newY, Rotation2d())
    }

    data class Coordinate(val x: Int, val y: Int)
}