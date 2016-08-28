package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.pfa.Connection
import com.badlogic.gdx.ai.pfa.DefaultConnection
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array

class TurnArea private constructor(val waypoints: List<WayPoint>, private val level: Level) : IndexedGraph<TurnArea.WayPoint> {

    private val pathFinder = IndexedAStarPathFinder<WayPoint>(this)

    data class WayPoint(val cell: Level.Cell, val index: Int, val center: Vector2) {
        val x: Int get() = cell.x
        val y: Int get() = cell.y

        val connections: Array<Connection<WayPoint>> = Array()
    }

    override fun getIndex(node: WayPoint): Int = node.index

    override fun getConnections(fromNode: WayPoint): Array<Connection<WayPoint>> = fromNode.connections

    override fun getNodeCount(): Int = waypoints.size

    fun getPath(fromX: Int, formY: Int, toX: Int, toY: Int, unitRadius: Float, smooth: Boolean): List<WayPoint> {
        val from = getWayPoint(fromX, formY)
        val to = getWayPoint(toX, toY)

        if (from == null || to == null) {
            return listOf()
        }

        val out: MySmoothableGraphPath = MySmoothableGraphPath()

        pathFinder.searchNodePath(from, to, ManhattanDistanceHeuristic(), out)

        if (smooth) {
            val pathSmoother = MyPathSmoother(MyRayCollisionDetector(level, unitRadius))
            pathSmoother.smoothPath(out)
        }

        return out.toList()
    }

    private fun getWayPoint(x: Int, y: Int): WayPoint? = waypoints.find { it.x == x && it.y == y }

    companion object {

        fun create(level: Level, startX: Int, startY: Int, unitSize: Int, maxDistance: Int): TurnArea {

            if (maxDistance <= 0 || unitSize <= 0) {
                return TurnArea(listOf(), level)
            }

            val frontier = mutableListOf<WayPoint>()
            val visited = mutableSetOf<WayPoint>()
            val distanceMap = mutableMapOf<WayPoint, Int>()

            val centerShift = unitSize * 0.5f

            fun extendFrontier(from: WayPoint?, x: Int, y: Int, distance: Int) {
                val neighbour = level.getCell(x, y)
                val alreadyVisited = visited.find { it.cell == neighbour } == null

                if (neighbour != null && alreadyVisited && !level.checkCellsIfOccupied(unitSize, neighbour.x, neighbour.y)) {
                    val wp = WayPoint(neighbour, visited.size, Vector2(neighbour.x + centerShift, neighbour.y + centerShift))
                    from?.connections?.add(DefaultConnection(from, wp))
                    frontier.add(wp)
                    visited.add(wp)
                    distanceMap[wp] = distance
                }
            }

            extendFrontier(null, startX, startY, maxDistance)

            while (frontier.isNotEmpty()) {
                val current = frontier.removeAt(0)

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