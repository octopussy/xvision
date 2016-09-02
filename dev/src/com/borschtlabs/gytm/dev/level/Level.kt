package com.borschtlabs.gytm.dev.level

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable
import com.borschtlabs.gytm.dev.Array2D

/**
 * @author octopussy
 */

class Level private constructor(val tiledMap: TiledMap) : Disposable {

    class Point {
        val loc: Vector2 = Vector2()
    }

    class Edge {
        var p0: Point? = null
        var p1: Point? = null
    }

    class Cell(x: Int, y: Int) : GridPoint2(x, y)  {
        var isWall: Boolean = false
        val edgesCCW: Array<Edge> = arrayOf()
    }

    val width: Int
    val height: Int
    val cellSize: Float

    private val cells: Array2D<Cell>

    init {
        width = tiledMap.properties.get("width") as Int
        height = tiledMap.properties.get("height") as Int
        val tileWidth = tiledMap.properties.get("tilewidth") as Int
        val tileHeight = tiledMap.properties.get("tileheight") as Int

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
                    if (getCell(x, y) != null) {
                        val cell = cells[x, y]
                        cell.isWall = true
                    }
                }
            }
        }
    }

    fun nearestIntersection(centerX: Float, centerY: Float, toX: Float, toY: Float, out: Vector2): Boolean {

        var nearestWall: Cell? = null

        GridRaytracer.trace(centerX.toDouble(), centerY.toDouble(), toX.toDouble(), toY.toDouble()) {
            x, y ->
            val cell = getCell(x, y)
            return@trace if (cell != null && cell.isWall) {
                nearestWall = cell
                true
            } else {
                false
            }
        }

        if (nearestWall == null) return false

        val xx = nearestWall!!.x.toFloat()
        val yy = nearestWall!!.y.toFloat()

        if (centerY < yy && Intersector.intersectSegments(centerX, centerY, toX, toY, xx, yy, xx + 1f, yy, out)) {
            return true
        }

        if (centerX > xx + 1f && Intersector.intersectSegments(centerX, centerY, toX, toY, xx + 1f, yy, xx + 1f, yy + 1f, out)) {
            return true
        }

        if (centerY > yy + 1f && Intersector.intersectSegments(centerX, centerY, toX, toY, xx, yy + 1f, xx + 1f, yy + 1f, out)) {
            return true
        }

        if (centerX < xx && Intersector.intersectSegments(centerX, centerY, toX, toY, xx, yy, xx, yy + 1f, out)) {
            return true
        }

        return false
    }

    override fun dispose() {
        tiledMap.dispose()
    }

    fun getCell(x: Int, y: Int): Cell? {
        if (x < 0 || x >= width) return null
        if (y < 0 || y >= height) return null
        return cells[x, y]
    }

    fun getWalls(swX: Float, swY: Float, neX: Float, neY: Float, list: MutableList<Cell>) {
        list.clear()
        val x0 = Math.max(swX.toInt(), 0)
        val y0 = Math.max(swY.toInt(), 0)
        val x1 = Math.min(neX.toInt(), width - 1)
        val y1 = Math.min(neY.toInt(), height - 1)

        for (y in y0..y1) {
            for (x in x0..x1) {
                val c = cells[x, y]
                if (c.isWall) {
                    list.add(c)
                }
            }
        }
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
