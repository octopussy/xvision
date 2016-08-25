package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.IntMap

/**
 * @author octopussy
 */

class Cell(val isWall: Boolean)

class Level private constructor(val width: Int, val height: Int, val cellSize: Int, val tiledMap: TiledMap): Disposable {

    private val cells: IntMap<Cell>

    init {
        cells = IntMap(width * height)

        val walls = tiledMap.layers.get("walls") as TiledMapTileLayer?
        walls?.apply {
            for (y in 0..height-1) {
                for (x in 0..width-1) {
                    val c = getCell(x, y)
                    if (c != null){

                    }

                    val cell = Cell(c != null)
                    cells.put(y * width + x, cell)
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
        return cells[y * width + x]
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
