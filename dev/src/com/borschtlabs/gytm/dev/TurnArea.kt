package com.borschtlabs.gytm.dev

/**
 * @author octopussy
 */

class TurnArea private constructor(val cells: Set<Cell>) {

    companion object {

        fun create(level: Level, startX: Int, startY: Int, unitSize: Int, maxDistance: Int): TurnArea {

            val startCell = level.getCell(startX, startY)

            if (startCell == null || maxDistance <= 0 || unitSize <= 0) {
                return createEmpty()
            }

            val visited = mutableSetOf<Cell>()
            val frontier = mutableListOf<Cell?>()
            val distanceMap = mutableMapOf<Cell, Int>()

            visited.add(startCell)
            frontier.add(startCell)
            distanceMap.put(startCell, maxDistance)

            fun extendFrontier(x: Int, y: Int, distance: Int) {
                val n = level.getCell(x, y)
                if (n != null && !visited.contains(n) && !level.checkCellsIfOccupied(unitSize, n.x, n.y)) {
                    frontier.add(n)
                    visited.add(n)
                    distanceMap[n] = distance
                }
            }

            while (frontier.isNotEmpty()) {
                val current = frontier.removeAt(0)

                if (current != null) {
                    val d = (distanceMap[current] ?: 0) - 1

                    if (d >= 0) {
                        // west
                        extendFrontier(current.x - 1, current.y, d)

                        // north
                        extendFrontier(current.x, current.y + 1, d)

                        // east
                        extendFrontier(current.x + 1, current.y, d)

                        // south
                        extendFrontier(current.x, current.y - 1, d)
                    }
                }
            }

            return TurnArea(visited)
        }

        private fun createEmpty(): TurnArea {
            throw UnsupportedOperationException("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }
}