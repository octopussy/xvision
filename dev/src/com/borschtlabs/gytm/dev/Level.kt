package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.utils.Disposable

/**
 * @author octopussy
 */

data class Cell(val x: Int, val y: Int) {
    var isWall: Boolean = false
}

class Level private constructor(val width: Int, val height: Int, val cellSize: Int, val tiledMap: TiledMap) : Disposable {

    private val cells: Array2D<Cell> = Array2D(width, height) { x, y -> Cell(x, y) }

    init {
        val walls = tiledMap.layers.get("walls") as TiledMapTileLayer?
        walls?.apply {
            for (y in 0..height - 1) {
                for (x in 0..width - 1) {
                    val c = getCell(x, y)

                    cells[x, y].isWall = c != null
                }
            }
        }
    }

    override fun dispose() {
        tiledMap.dispose()
    }

    fun getCell(x: Int, y: Int): Cell? {
        if (x < 0 || x >= width) return null
        if (y < 0 || y >= height) return null
        return cells[x, y]
    }

    private fun checkCellsUnderWaypoint(size: Int, x: Int, y: Int): Boolean {
        for (yy in y..y + size - 1) {
            for (xx in x..x + size - 1) {
                if (xx >= width || yy >= height) {
                    return true
                }

                val c = cells[xx, yy]
                if (c.isWall) {
                    return true
                }
            }
        }
        return false
    }

    companion object {
        val TAG = "Level"

        fun createBlank(width: Int, height: Int, tileWidth: Int, tileHeight: Int, tiledMap: TiledMap): Level {
            if (width <= 0 || height <= 0) {
                val msg = "Level measures are negative! (w=$width, h=$height)"
                Gdx.app.error(TAG, msg)
                throw IllegalArgumentException(msg)
            }

            if (tileWidth <= 0 || tileHeight <= 0) {
                val msg = "Level tile measures are negative! (w=$tileWidth, h=$tileHeight)"
                Gdx.app.error(TAG, msg)
                throw IllegalArgumentException(msg)
            }

            if (tileWidth != tileHeight) {
                val msg = "Level tile is not a square! (w=$tileWidth, h=$tileHeight)"
                Gdx.app.error(TAG, msg)
                throw IllegalArgumentException(msg)
            }

            return Level(width, height, tileWidth, tiledMap)
        }
    }

    fun getTurnArea(startX: Int, startY: Int, unitSize: Int, maxDistance: Int): List<Cell> {

        val startCell = getCell(startX, startY) ?: return listOf()

        if (maxDistance <= 0 || unitSize <= 0) {
            return listOf(startCell)
        }

        val visited = mutableSetOf<Cell>()
        val frontier = mutableListOf<Cell?>()
        val distanceMap = mutableMapOf<Cell, Int>()

        frontier.add(startCell)
        distanceMap.put(startCell, maxDistance)

        fun extendFrontier(x: Int, y: Int, distance: Int) {
            val n = getCell(x, y)
            if (n != null && !visited.contains(n) && !checkCellsUnderWaypoint(unitSize, n.x, n.y)) {
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

        return visited.toList()
    }
}
