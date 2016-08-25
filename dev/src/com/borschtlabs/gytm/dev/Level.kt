package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.IntMap

/**
 * @author octopussy
 */

interface Cell {
    val isWall: Boolean
}

data class Waypoint(val x: Float, val y: Float)

class NavGrid(val size: Int) {
    val waypoints: MutableList<Waypoint> = mutableListOf()
}

class Level private constructor(val width: Int, val height: Int, val cellSize: Int, val tiledMap: TiledMap) : Disposable {

    private class CellImpl : Cell {
        var _isWall = false
        override val isWall: Boolean get() = _isWall
    }

    private val cells: Array2D<Cell> = Array2D(width, height) { x, y -> CellImpl() }

    val navGrids: IntMap<NavGrid> = IntMap()

    init {
        val walls = tiledMap.layers.get("walls") as TiledMapTileLayer?
        walls?.apply {
            for (y in 0..height - 1) {
                for (x in 0..width - 1) {
                    val c = getCell(x, y)

                    (cells[x, y] as CellImpl)._isWall = c != null
                }
            }
        }

        initNavGrid(1)
        initNavGrid(2)
        initNavGrid(3)
    }

    override fun dispose() {
        tiledMap.dispose()
    }

    fun getCell(x: Int, y: Int): Cell? {
        if (x < 0 || x >= width) return null
        if (y < 0 || y >= height) return null
        return cells[x, y]
    }

    private fun initNavGrid(size: Int) {
        if (size <= 0) return

        val grid: NavGrid = NavGrid(size)
        navGrids.put(size, grid)

        val hs = size.toFloat() / 2f

        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                val passable = checkCellsUnderWaypoint(size, x, y)

                if (!passable) {
                    val wp = Waypoint(x + hs, y + hs)
                    grid.waypoints.add(wp)
                }
            }
        }
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
}
