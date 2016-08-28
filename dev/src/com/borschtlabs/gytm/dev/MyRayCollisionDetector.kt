package com.borschtlabs.gytm.dev

import com.badlogic.gdx.ai.utils.Collision
import com.badlogic.gdx.ai.utils.Ray
import com.badlogic.gdx.ai.utils.RaycastCollisionDetector
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2

/**
 * @author octopussy
 */

class MyRayCollisionDetector(private val level: Level, private val unitRadius: Float) : RaycastCollisionDetector<Vector2> {

    val proximityDistance = Math.sqrt(0.5)

    override fun collides(ray: Ray<Vector2>): Boolean = collides(null, ray)

    override fun findCollision(outputCollision: Collision<Vector2>, inputRay: Ray<Vector2>): Boolean = collides(outputCollision, inputRay)

    private fun collides(out: Collision<Vector2>?, centralRay: Ray<Vector2>): Boolean {
        val tan = Vector2(-(centralRay.end.y - centralRay.start.y), centralRay.end.x - centralRay.start.x).nor().scl(unitRadius)

        if (collidesRay(out, Ray<Vector2>(
                Vector2(centralRay.start.x + tan.x, centralRay.start.y + tan.y),
                Vector2(centralRay.end.x + tan.x, centralRay.end.y + tan.y)))){
            return true
        }

        if (collidesRay(out, Ray<Vector2>(
                Vector2(centralRay.start.x - tan.x, centralRay.start.y - tan.y),
                Vector2(centralRay.end.x - tan.x, centralRay.end.y - tan.y)))){
            return true
        }

        return false
    }

    private fun collidesRay(out: Collision<Vector2>?, ray: Ray<Vector2>): Boolean {
        val minX = Math.floor(Math.min(ray.start.x, ray.end.x).toDouble()).toInt()
        val minY = Math.floor(Math.min(ray.start.y, ray.end.y).toDouble()).toInt()
        val maxX = Math.ceil(Math.max(ray.start.x, ray.end.x).toDouble()).toInt()
        val maxY = Math.ceil(Math.max(ray.start.y, ray.end.y).toDouble()).toInt()

        val cells = mutableListOf<Level.Cell>()
        for (y in minY..maxY) {
            for (x in minX..maxX) {
                val cell = level.getCell(x, y)
                if (cell != null && cell.isWall) {
                    val distance = Intersector.distanceLinePoint(
                            ray.start.x, ray.start.y,
                            ray.end.x, ray.end.y,
                            cell.x + 0.5f, cell.y + 0.5f)

                    if (distance < proximityDistance) {
                        cells.add(cell)
                    }
                }
            }
        }

        var hasIntersection = false

        val nearestIntersection: Vector2 = Vector2(Float.MAX_VALUE, Float.MAX_VALUE)
        var nearestDistance = Float.MAX_VALUE

        val detectNearestIntersection = false
        val intersectionVector: Vector2? = Vector2()

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
}