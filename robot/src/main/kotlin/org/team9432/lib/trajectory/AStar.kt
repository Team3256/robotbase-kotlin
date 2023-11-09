package org.team9432.lib.trajectory

import edu.wpi.first.math.geometry.Pose2d
import org.littletonrobotics.junction.Logger
import org.team9432.lib.field.*
import java.time.Instant
import kotlin.math.abs
import kotlin.math.sqrt
import org.team9432.lib.field.ChargedUp2023.navigationWaypoints as waypoints

/**
 * A* implementation from https://rosettacode.org/wiki/A*_search_algorithm#Kotlin
 *
 * The "resolution" of the grid can be defined with the precision parameter
 * Higher values will take longer, but the path will be defined in more/smaller steps and is therefore more precise
 * Default value is 4
 */
class AStar(private vararg val obstacles: Region) {
    /** This function takes Pose2d positions and converts them into a grid system that is usable by A* */
    fun findPath(initialPose: Pose2d, finalPose: Pose2d, printTime: Boolean = false): List<Pose2d>? {
        val start = if (printTime) Instant.now() else null

        // Find the nearest waypoint to the start and end poses
        val initialCoordinate = waypoints.minBy { heuristicDistance(it, initialPose.toPoint()) }
        val finalCoordinate = waypoints.minBy { heuristicDistance(it, finalPose.toPoint()) }

        val (path, cost) = searchAStar(initialCoordinate, finalCoordinate) ?: return null

        val waypoints = path.map { (it.toPose2d()) }.toMutableList()
        waypoints.add(finalPose)
        waypoints.add(0, initialPose)

        val optimizedPath = optimizePath(waypoints.map { it.toPoint() }).map { it.toPose2d() }

        Logger.recordOutput("Planner/WaypointPath", *waypoints.toTypedArray())

        if (printTime) println("Path generation time: ${Instant.now().toEpochMilli() - start!!.toEpochMilli()}ms")

//        println("Start: (x: ${initialCoordinate.x}, y: ${initialCoordinate.y}) End: (x: ${finalCoordinate.x}, y: ${finalCoordinate.y}) Cost: $cost Path: $path")
        return optimizedPath
    }

    /**
     * Method to remove extra waypoints from a generated path.
     * Starting with the path points ABCDE it checks: Is there a straight line from A to C? If so, discard B and check A to D, etc. If not, start the algorithm again from point B
     */
    private fun optimizePath(waypoints: List<Point>): List<Point> {
        val newPath = waypoints.toMutableList()
        main@ for (i in 0..<newPath.size) {
            val a = newPath.getOrNull(i) ?: break

            do {
                val c = newPath.getOrNull(i + 2) ?: break@main
                val line = Line(a, c)
                val blocked = obstacles.any { it.intersects(line) }
                if (!blocked) newPath.removeAt(i + 1)
            } while (!blocked)
        }

        return newPath
    }

    /**
     * Implementation of the A* Search Algorithm to find the optimum path between 2 points on a grid.
     *
     * The Grid contains the details of the barriers and methods which supply the neighboring vertices and the cost of movement between 2 cells.
     */
    private fun searchAStar(start: Point, finish: Point): Pair<List<Point>, Double>? {
        /** Use the cameFrom values to backtrack to the start position to generate the path */
        fun generatePath(currentPos: Point, cameFrom: Map<Point, Point>): List<Point> {
            val path = mutableListOf(currentPos)
            var current = currentPos
            while (cameFrom.containsKey(current)) {
                current = cameFrom.getValue(current)
                path.add(0, current)
            }
            return path.toList()
        }

        val openVertices = mutableSetOf(start)
        val closedVertices = mutableSetOf<Point>()
        val costFromStart = mutableMapOf(start to 0.0)
        val estimatedTotalCost = mutableMapOf(start to heuristicDistance(start, finish))

        val cameFrom = mutableMapOf<Point, Point>() // Used to generate path by back tracking

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

    // Any waypoint can be moved to so long as there isn't an obstacle in the way
    private fun getNeighbors(coordinate: Point) = waypoints.filterNot { obstacles.any { o -> o.intersects(Line(it, coordinate)) } }

    /** Gets the distance between two points */
    private fun heuristicDistance(start: Point, finish: Point): Double {
        val dx = abs(start.x - finish.x)
        val dy = abs(start.y - finish.y)
        return sqrt((dx * dx) + (dy * dy)) // Triangles! A^2 + B^2 = C^2
    }

    private fun getMoveCost(from: Point, to: Point): Double {
        // Basically, if the line goes through a wall, don't follow it. If not, the cost is the length of the line.
        val path = Line(Point(from.x, from.y), Point(to.x, to.y))
        if (obstacles.any { it.intersects(path) }) return Double.MAX_VALUE
        return heuristicDistance(from, to)
    }
}