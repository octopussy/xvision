package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.PathSmoother
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class TurnArea private constructor(val waypoints: List<WayPoint>, level: Level) : IndexedGraph<TurnArea.WayPoint> {

    private val pathFinder = IndexedAStarPathFinder<WayPoint>(this)
    private val pathSmoother = PathSmoother<WayPoint, Vector2>(level)

    class WayPoint(val cell: Level.Cell, val index: Int, val center: Vector2) {
        val x: Int get() = cell.x
        val y: Int get() = cell.y

        val connections: Array<Connection<WayPoint>> = Array()
    }

    override fun getIndex(node: WayPoint): Int = node.index

    override fun getConnections(fromNode: WayPoint): Array<Connection<WayPoint>> = fromNode.connections

    override fun getNodeCount(): Int = waypoints.size

    fun getPath(fromX: Int, formY: Int, toX: Int, toY: Int): List<WayPoint> {
        val from = getWayPoint(fromX, formY)
        val to = getWayPoint(toX, toY)

        val out: MySmoothableGraphPath = MySmoothableGraphPath()

        pathFinder.searchNodePath(from, to, ManhattanDistanceHeuristic(), out)

        pathSmoother.smoothPath(out)

        return out.toList()
    }

    private fun getWayPoint(x: Int, y: Int): WayPoint? = waypoints.find { it.x == x && it.y == y }

    companion object {

        fun create(level: Level, startX: Int, startY: Int, unitSize: Int, maxDistance: Int): TurnArea {

            val startCell = level.getCell(startX, startY)

            if (startCell == null || maxDistance <= 0 || unitSize <= 0) {
                return TurnArea(listOf(), level)
            }

            val frontier = mutableListOf<WayPoint?>()

            val visited = mutableSetOf<WayPoint>()
            val distanceMap = mutableMapOf<WayPoint, Int>()

            val centerShift = (unitSize - 1) * 0.5f

            var current = WayPoint(startCell, 0, Vector2(startCell.x + centerShift, startCell.y + centerShift))

            visited.add(current)
            frontier.add(current)
            distanceMap.put(current, maxDistance)

            fun extendFrontier(from: WayPoint, x: Int, y: Int, distance: Int) {
                val n = level.getCell(x, y)
                val alreadyVisited = visited.find { it.cell == n } == null
                if (n != null && alreadyVisited && !level.checkCellsIfOccupied(unitSize, n.x, n.y)) {
                    val wp = WayPoint(n, visited.size, Vector2(n.x + centerShift, n.y  + centerShift))

                    from.connections.add(DefaultConnection(from, wp))

                    frontier.add(wp)
                    visited.add(wp)
                    distanceMap[wp] = distance
                }
            }

            while (frontier.isNotEmpty()) {
                current = frontier.removeAt(0)!!

                val d = (distanceMap[current] ?: 0) - 1

                if (d >= 0) {
                    // west
                    extendFrontier(current, current.x - 1, current.y, d)

                    // north
                    extendFrontier(current, current.x, current.y + 1, d)

                    // east
                    extendFrontier(current, current.x + 1, current.y, d)

                    // south
                    extendFrontier(current, current.x, current.y - 1, d)
                }
            }

            return TurnArea(visited.toList(), level)
        }

    }
}