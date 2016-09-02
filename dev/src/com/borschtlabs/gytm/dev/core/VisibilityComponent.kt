package com.borschtlabs.gytm.dev.core

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Pool
import com.badlogic.gdx.utils.Pools
import com.borschtlabs.gytm.dev.level.Level

/**
 * @author octopussy
 */

class Point : Pool.Poolable {

    val position: Vector2 = Vector2()

    var angleFromCenter: Float = 0f

    override fun reset() {
        position.set(0f, 0f)
        angleFromCenter = 0f
    }

    override fun hashCode(): Int = this.position.hashCode()

    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other is Point) {
            return this.position.equals(other.position)
        } else {
            return false
        }
    }
}

class VisibilityComponent(owner: Actor) : ActorComponent(owner) {

    private val FAR_DISTANCE = 1000f

    private val DISTANCE_EPSILON = 0.0001f

    private val ANGLE_DEVIATION = 0.000001f

    class DebugGraphics {
        var color: Color = Color.BLUE
        val lines = mutableListOf<Pair<Vector2, Vector2>>()
    }

    private var swX: Float = 0f
    private var swY: Float = 0f
    private var neX: Float = 0f
    private var neY: Float = 0f

    var isEnabled: Boolean = true

    var maxDistance = 50.0f

    var showDebug = true

    val resultPoints = Array<Point>()

    val debugInfo = mutableListOf<DebugGraphics>()

    private val allPoints = hashSetOf<Point>()

    override fun tick(dt: Float) {
        super.tick(dt)

        location.set(owner.location)

        if (!isEnabled) return

        calculateVisibility()
    }

    val walls = mutableListOf<Level.Cell>()

    private fun nearestIntersection(centerX: Float, centerY: Float, toX: Float, toY: Float, out: Vector2) {
        if (owner.world.level.nearestIntersection(centerX, centerY, toX, toY, out)) {
            return
        }

        if (Intersector.intersectSegments(centerX, centerY, toX, toY, swX, swY, swX, neY, out)) {
            return
        }

        if (Intersector.intersectSegments(centerX, centerY, toX, toY, swX, neY, neX, neY, out)) {
            return
        }

        if (Intersector.intersectSegments(centerX, centerY, toX, toY, neX, neY, neX, swY, out)) {
            return
        }

        if (Intersector.intersectSegments(centerX, centerY, toX, toY, neX, swY, swX, swY, out)) {
            return
        }
    }

    private fun calculateVisibility() {
        allPoints.forEach { Pools.free(it) }
        allPoints.clear()

        Pools.freeAll(resultPoints)
        resultPoints.clear()

        debugInfo.clear()

        val centerX = location.x
        val centerY = location.y

        val level = owner.world.level

        swX = (Math.round(centerX) - maxDistance)
        swY = (Math.round(centerY) - maxDistance)
        neX = (swX + maxDistance * 2)
        neY = (swY + maxDistance * 2)

        val tmp: Vector2 = Vector2()

        addPointToAll(swX, swY)
        addPointToAll(neX, swY)
        addPointToAll(neX, neY)
        addPointToAll(swX, neY)

        level.getWalls(swX, swY, neX, neY, walls)

        walls.forEach {
            val x = it.x
            val y = it.y
            val fx = it.x.toFloat()
            val fy = it.y.toFloat()

            if (centerY < fy && !checkWall(x, y - 1)) {
                addPointToAll(fx, fy)
                addPointToAll(fx + 1f, fy)
            }

            if (centerX > fx + 1f && !checkWall(x + 1, y)) {
                addPointToAll(fx + 1f, fy)
                addPointToAll(fx + 1f, fy + 1f)
            }

            if (centerY > fy + 1f && !checkWall(x, y + 1)) {
                addPointToAll(fx + 1f, fy + 1f)
                addPointToAll(fx, fy + 1f)
            }

            if (centerX < fx && !checkWall(x - 1, y)) {
                addPointToAll(fx, fy + 1f)
                addPointToAll(fx, fy)
            }
        }

        val dir = Vector2()
        val farPoint: Vector2 = Vector2()

        allPoints.forEach {
            // trace straight
            dir.set(it.position.x - centerX, it.position.y - centerY).setLength(FAR_DISTANCE)
            farPoint.set(centerX + dir.x, centerY + dir.y)

            var mainPointAdded = false
            val distanceToMainPoint = Vector2.len(centerX - it.position.x, centerY - it.position.y)
            nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
            val nearestDistance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
            if (nearestDistance + DISTANCE_EPSILON >= distanceToMainPoint) {
                addPoint(it.position.x, it.position.y, dir.angleRad())
                mainPointAdded = true
            }


            if (mainPointAdded) {
                // trace left -0.0001 rad

                dir.set(it.position.x - centerX, it.position.y - centerY).rotateRad(ANGLE_DEVIATION).setLength(FAR_DISTANCE)
                farPoint.set(centerX + dir.x, centerY + dir.y)

                nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
                var distance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
                if (distance + DISTANCE_EPSILON >= distanceToMainPoint) {
                    addPoint(tmp.x, tmp.y, dir.angleRad())
                }


                // trace right +0.0001 rad
                dir.set(it.position.x - centerX, it.position.y - centerY).rotateRad(-ANGLE_DEVIATION).setLength(FAR_DISTANCE)
                farPoint.set(centerX + dir.x, centerY + dir.y)

                nearestIntersection(centerX, centerY, farPoint.x, farPoint.y, tmp)
                distance = Vector2.len(tmp.x - centerX, tmp.y - centerY)
                if (distance + DISTANCE_EPSILON >= distanceToMainPoint) {
                    addPoint(tmp.x, tmp.y, dir.angleRad())
                }
            }

        }

        resultPoints.sort { p1, p2 ->
            when {
                p1.angleFromCenter < p2.angleFromCenter -> 1
                p1.angleFromCenter > p2.angleFromCenter -> -1
                else -> 0
            }
        }

        addPoint(resultPoints[0].position.x, resultPoints[0].position.y, resultPoints[0].angleFromCenter)
    }

    private fun addPointToAll(x: Float, y: Float) {
        val p = Pools.obtain(Point::class.java)
        p.position.set(x, y)
        allPoints.add(p)
    }

    private fun checkWall(x: Int, y: Int): Boolean {
        val c = owner.world.level.getCell(x, y)
        return c != null && c.isWall
    }

    private fun addPoint(x: Float, y: Float, angle: Float) {
        val p = Pools.obtain(Point::class.java)
        p.position.set(x, y)
        p.angleFromCenter = angle
        resultPoints.add(p)
    }
}