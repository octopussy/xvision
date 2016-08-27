package com.borschtlabs.gytm.dev

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.utils.Collision
import com.badlogic.gdx.ai.utils.Ray
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer
import com.badlogic.gdx.math.GridPoint2
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Disposable

/**
 * @author octopussy
 */

class Level private constructor(val width: Int, val height: Int, val cellSize: Int, val tiledMap: TiledMap) :
        RaycastCollisionDetector<Vector2>, Disposable {

    class Cell(x: Int, y: Int) : GridPoint2(x, y) {
        var isWall: Boolean = false
    }

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

    // TODO: optimize

    val intersectionMinDistance = 0.5
    val proximityDistance = Math.sqrt(intersectionMinDistance)

    override fun collides(ray: Ray<Vector2>): Boolean = collides(null, ray)

    override fun findCollision(outputCollision: Collision<Vector2>, inputRay: Ray<Vector2>): Boolean = collides(outputCollision, inputRay)

    private fun collides(out: Collision<Vector2>?, ray: Ray<Vector2>): Boolean {

        val minX = Math.floor(Math.min(ray.start.x, ray.end.x).toDouble()).toInt()
        val minY = Math.floor(Math.min(ray.start.y, ray.end.y).toDouble()).toInt()
        val maxX = Math.ceil(Math.max(ray.start.x, ray.end.x).toDouble()).toInt()
        val maxY = Math.ceil(Math.max(ray.start.y, ray.end.y).toDouble()).toInt()

        val cells = mutableListOf<Cell>()
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val cell = getCell(x, y)
                if (cell != null && cell.isWall) {
                    cells.add(cell)
                }
            }
        }

        var hasIntersection = false

        val nearestIntersection: Vector2 = Vector2(Float.MAX_VALUE, Float.MAX_VALUE)
        var nearestDistance = Float.MAX_VALUE

        val detectNearestIntersection = out != null
        val intersectionVector: Vector2? = if (detectNearestIntersection) Vector2() else null

        val checkEdge: (Float, Float, Float, Float) -> Unit = {
            x1, y1, x2, y2 ->
            val result = Intersector.intersectSegments(ray.start.x, ray.start.y, ray.end.x, ray.end.y,
                    x1, y1, x2, y2, intersectionVector)

            if (result && detectNearestIntersection && intersectionVector != null) {
                val dist = ray.start.dst2(intersectionVector)
                if (dist < nearestDistance) {
                    nearestDistance = dist
                    nearestIntersection.set(intersectionVector)
                }
            }

            if (!hasIntersection && result) {
                hasIntersection = true
            }
        }

        for (c in cells) {

            val x = c.x.toFloat()
            val y = c.y.toFloat()

            checkEdge(x, y + 1f, x + 1f, y + 1f)
            checkEdge(x + 1f, y, x + 1f, y + 1f)
            checkEdge(x, y, x + 1f, y)
            checkEdge(x, y, x, y + 1f)

            if (!detectNearestIntersection && hasIntersection) {
                return true
            }
        }

        return hasIntersection
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
