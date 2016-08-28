package com.borschtlabs.gytm.dev.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.utils.Disposable
import com.borschtlabs.gytm.dev.Array2D

/**
 * @author octopussy
 */

class Level private constructor(val tiledMap: TiledMap) : Disposable {

    class Cell(x: Int, y: Int) : GridPoint2(x, y) {
        var isWall: Boolean = false
    }

    val width: Int
    val height: Int
    val cellSize: Float

    private val cells: Array2D<Cell>

    init {
        width = tiledMap.properties.get("width") as Int
        height = tiledMap.properties.get("height") as Int
        val tileWidth = tiledMap.properties.get("tilewidth") as Int
        val tileHeight = tiledMap.properties.get("tileheight")  as Int

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

        cellSize = tileWidth.toFloat()

        val groundLayer = tiledMap.layers.get("ground")

        if (groundLayer == null) {
            val msg = "Level has no 'ground' layer."
            Gdx.app.error(TAG, msg)
            throw IllegalArgumentException(msg)
        }

        cells = Array2D(width, height) { x, y -> Cell(x, y) }

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

    fun checkCellsIfOccupied(size: Int, x: Int, y: Int): Boolean {
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

        fun create(tiledMap: TiledMap): Level {
            return Level(tiledMap)
        }
    }
}
